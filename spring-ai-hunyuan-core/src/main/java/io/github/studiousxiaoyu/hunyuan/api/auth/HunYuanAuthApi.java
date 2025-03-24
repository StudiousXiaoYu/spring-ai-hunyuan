package io.github.studiousxiaoyu.hunyuan.api.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.ModelOptionsUtils;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanConstants;
import org.springframework.http.HttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.studiousxiaoyu.hunyuan.api.HunYuanConstants.CT_JSON;

/**
 * The HunYuanAuthApi class is responsible for handling authentication-related operations
 * for the HunYuan API. It provides methods to generate necessary headers and signatures
 * required for authenticated requests.
 *
 * @author Your Name
 */
public class HunYuanAuthApi {

	private static final Logger logger = LoggerFactory.getLogger(HunYuanAuthApi.class);

	private final static Charset UTF8 = StandardCharsets.UTF_8;
	private final static String VERSION = HunYuanConstants.DEFAULT_VERSION;
	private final static String ALGORITHM = HunYuanConstants.DEFAULT_ALGORITHM;

	private final String secretId;

	private final String secretKey;
	private final String host;
	private final String service;

	/**
	 * Constructs a HunYuanAuthApi instance with the specified secret ID and secret key.
	 * @param secretId The secret ID used for authentication.
	 * @param secretKey The secret key used for authentication.
	 */
	public HunYuanAuthApi(String secretId, String secretKey, String host,String service) {
		this.secretId = secretId;
		this.secretKey = secretKey;
		this.host = host;
		this.service = service;
	}

	/**
	 * Generates an HMAC-SHA256 signature using the provided key and message.
	 * @param key The key used for generating the HMAC-SHA256 signature.
	 * @param msg The message to be signed.
	 * @return The byte array of the generated HMAC-SHA256 signature.
	 */
	public byte[] hmac256(byte[] key, String msg) {
		Mac mac = null;
		try {
			mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
			mac.init(secretKeySpec);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return mac.doFinal(msg.getBytes(UTF8));
	}

	/**
	 * Computes the SHA-256 hash of the provided string and returns it as a hexadecimal
	 * string.
	 * @param s The string to be hashed.
	 * @return The SHA-256 hash of the input string in hexadecimal format.
	 */
	public String sha256Hex(String s) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] d = md.digest(s.getBytes(UTF8));
		return DatatypeConverter.printHexBinary(d).toLowerCase();
	}

	/**
	 * Generates the HTTP headers required for making authenticated requests to the
	 * HunYuan API.
	 * @return A MultiValueMap containing the HTTP headers needed for the authenticated
	 * request.
	 */
	public MultiValueMap<String, String> getHttpHeadersConsumer(String action, byte[] body) {
		// String timestamp = "1551113065";
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// Pay attention to the time zone, otherwise it will be easy to make mistakes
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String date = sdf.format(new Date(Long.valueOf(timestamp + "000")));

		// ************* Step 1: Splice specification request strings *************
		String httpRequestMethod = "POST";
		String canonicalUri = "/";
		String canonicalQueryString = "";
		String canonicalHeaders = "content-type:application/json; charset=utf-8\n" + "host:" + host + "\n"
				+ "x-tc-action:" + action.toLowerCase() + "\n";
		String signedHeaders = "content-type;host;x-tc-action";

		// String payload = "{\"Limit\": 1, \"Filters\": [{\"Values\":
		// [\"\\u672a\\u547d\\u540d\"], \"Name\": \"instance-name\"}]}";
		String hashedRequestPayload = sha256Hex(new String(body, UTF8));
		String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n"
				+ canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;
		// ************* Step 2: Splice the string to be signed *************
		String credentialScope = date + "/" + service + "/" + "tc3_request";
		String hashedCanonicalRequest = sha256Hex(canonicalRequest);
		String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;
		// ************* Step 3: Calculate the signature *************
		byte[] secretDate = hmac256(("TC3" + secretKey).getBytes(UTF8), date);
		byte[] secretService = hmac256(secretDate, service);
		byte[] secretSigning = hmac256(secretService, "tc3_request");
		String signature = DatatypeConverter.printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase();
		// ************* Step 4: Splice Authorization *************
		String authorization = ALGORITHM + " " + "Credential=" + secretId + "/" + credentialScope + ", "
				+ "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;

		TreeMap<String, String> headers = new TreeMap<String, String>();
		headers.put("Authorization", authorization);
		headers.put("Content-Type", CT_JSON);
		// headers.put("Host", host);
		headers.put("X-TC-Action", action);
		headers.put("X-TC-Timestamp", timestamp);
		headers.put("X-TC-Version", VERSION);

		return CollectionUtils.toMultiValueMap(
				headers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> List.of(e.getValue()))));
	}

	public MultiValueMap<String, String> getHttpHeadersConsumerByRequest(String action, HunYuanApi.ChatCompletionRequest chatRequest) {
		return getHttpHeadersConsumer(action,ModelOptionsUtils.toJsonString(chatRequest).getBytes(UTF8));
	}

}
