package auraya;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import auraya.v8.body.CrossMatch;
import auraya.v8.body.Enrolment;
import auraya.v8.body.Gender;
import auraya.v8.body.ModelRank;
import auraya.v8.body.Quality;
import auraya.v8.body.Similarity;
import auraya.v8.body.Utterance;
import auraya.v8.body.Verification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

/**
 * Builds JSON request objects for Armorvox API version 8, 
 * then sends them to the configured server endpoint and prints the response.
 * 
 * Can be configured to show request and response JSON objects pretty printed for clarity.
 * 
 * Each v8 API call is represented by a corresponding public method in this class.
 * 
 * @author Jamie Lister
 *
 */
public class ArmorvoxClient {
	
	
	private String server;
	private String group;
	private SSLContext sslContext;
	private boolean showRequest = false;
	private final ObjectMapper mapper = new ObjectMapper();
	
	@AllArgsConstructor
	@Data
	public static class UtteranceParameters {
		 String filepath;
		 Boolean checkQuality;
		 String phrase;
		 String vocab;
		 Boolean isRecognition;
	}
	
	@SneakyThrows
	public ArmorvoxClient(String server, String group, boolean isPrettyPrint, boolean showRequest) {
		this.server = server;
		this.group = group;
		this.sslContext = SSLContext.getInstance("TLS");
		this.sslContext.init(null, null, null);
		this.showRequest = showRequest;
		
		if (isPrettyPrint) {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
			mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
		}
	}
	

	
	@SneakyThrows
	public void enrol(String id, String printName, UtteranceParameters[] utterances, String channel, String overrides) {
		
		List<Utterance> jsonUtterances = getUtterances(utterances);
		
		Enrolment enrolment = new Enrolment();
		enrolment.setUtterances(jsonUtterances);
		enrolment.setChannel(channel);
		enrolment.setOverride(overrides);
		
		sendAndReceive(format("/voiceprint/%s/%s", id, printName), "POST", enrolment);
	}
	
	@SneakyThrows
	public void verify(String id, String printName, UtteranceParameters utterance, String channel, String overrides) {
		
		Utterance jsonUtterance = getUtterance(utterance);
		
		Verification verification = new Verification();
		verification.setUtterance(jsonUtterance);
		verification.setChannel(channel);
		verification.setOverride(overrides);
		
		sendAndReceive(format("/voiceprint/%s/%s", id, printName), "PUT", verification);
	}
	

	@SneakyThrows
	public void delete(String id, String printName) {
		sendAndReceive(format("/voiceprint/%s/%s", id, printName), "DELETE", null);
	}
	
	public void getVoicePrint(String id, String printName, boolean noPayload) {
		sendAndReceive(format("/voiceprint/%s/%s?no_payload=%s", id, printName, noPayload), "GET", null);
	}

	public void getPhrase(String vocab) {
		sendAndReceive(format("/phrase/%s", vocab), "GET", null);
	}

	public void checkHealth() {
		sendAndReceive("/health", "GET", null);
	}

	public void checkQuality(String printName, UtteranceParameters utterance, String mode, String channel, String overrides) {
		Utterance jsonUtterance = getUtterance(utterance);
		
		Quality quality = new Quality();
		quality.setUtterance(jsonUtterance);
		quality.setChannel(channel);
		quality.setOverride(overrides);
		quality.setMode(mode);
		
		sendAndReceive(format("/analysis/quality/%s", printName), "POST", quality);
	}


	public void crossMatch(String[] ids, String printName, UtteranceParameters utterance, String channel, String overrides) {
		Utterance jsonUtterance = getUtterance(utterance);
		
		CrossMatch crossMatch = new CrossMatch();
		crossMatch.setIds(Arrays.asList(ids));
		crossMatch.setUtterance(jsonUtterance);
		crossMatch.setChannel(channel);
		crossMatch.setOverride(overrides);
		
		sendAndReceive(format("/voiceprint/%s", printName), "PUT", crossMatch);
	}
	

	public void detectGender(UtteranceParameters[] utterances, String overrides) {
		List<Utterance> jsonUtterances = getUtterances(utterances);
		
		Gender gender = new Gender();
		gender.setUtterances(jsonUtterances);
		gender.setOverride(overrides);
		
		sendAndReceive(format("/analysis/gender"), "POST", gender);
	}
	

	public void similariy(UtteranceParameters[] utts, String overrides) {
		List<Utterance> jsonUtterances = getUtterances(utts);
		
		Similarity similarity = new Similarity();
		similarity.setUtterances(jsonUtterances);
		similarity.setOverride(overrides);
		
		sendAndReceive(format("/analysis/similarity"), "POST", similarity);
	}



	public void modelRank(UtteranceParameters[] utts, String[] ubmNames, String overrides) {
		List<Utterance> jsonUtterances = getUtterances(utts);
		
		ModelRank modelRank = new ModelRank();
		modelRank.setUtterances(jsonUtterances);
		modelRank.setUbmNames(Arrays.asList(ubmNames));
		modelRank.setOverride(overrides);
		

		sendAndReceive(format("/analysis/model_rank"), "POST", modelRank);
	}



	/**
	 * Formats arguments for safe use in URL
	 * 
	 * @param s string with format characters to replace encoded arguments
	 * @param args The arguments to encode
	 * @return The formatted string containing encoded arguments
	 */
	@SneakyThrows
	private String format(String s, Object... args) {
		for (int i = 0; i < args.length; i++) {
			args[i] = URLEncoder.encode(args[i].toString(), "UTF-8");
		}
		return String.format(s, args);
	}

	
	/**
	 * Builds a list of API v8 utterance objects from UtteranceParameters object

	 * @param utterances The array of utterances and parameters
	 * @return The list of 'Jackson ready' utterances
	 */
	private List<Utterance> getUtterances(UtteranceParameters[] utterances) {
		List<Utterance> result = new ArrayList<>();
		for (UtteranceParameters u : utterances) {
			result.add(getUtterance(u));
		}
		return result;
	}
	
	/**
	 * 
	 * @param up An UtteranceParameter object containing a path to an utterance and any parameters
	 * @return A 'Jackson ready' API v8 utterance object
	 */
	@SneakyThrows
	private Utterance getUtterance(UtteranceParameters up) {
		byte[] bytes = Files.readAllBytes(Paths.get(up.filepath));		
		return new Utterance(bytes, up.getPhrase(), up.getVocab(), null, up.getCheckQuality(), up.getIsRecognition());
	}

	/**
	 * 
	 * @param requestPath URL path to RESTful service
	 * @param method HTTP method (GET, POST, PUT, DELETE)
	 * @param bodyObject The 'Jackson ready' object to deserialise
	 */
	@SneakyThrows
	private void sendAndReceive(String requestPath, String method, Object bodyObject) {
		
		
		String path = String.format("%s%s", server, requestPath);
		System.out.printf("%s %s%n", method, path);
		
		// Open connection
		URL url = new URL(path);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		connection.setRequestProperty("Authorization", group);
		
		// Setup SSL if necessary
	    if ("https".equalsIgnoreCase(url.getProtocol())) {
	        ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
	    }
	    
	    long startTime = System.nanoTime();
	    
	    // Send object body
	    if (bodyObject != null) {
	    	
	    	String body = mapper.writeValueAsString(bodyObject);
			if (this.showRequest) {
				System.out.println();
				System.out.println("Request body:");
				System.out.println(body);
			}
	    	
			connection.setDoOutput(true);
			OutputStream os = connection.getOutputStream();
			

		    startTime = System.nanoTime();
			os.write(body.getBytes(Charset.defaultCharset()));
			os.flush();
			os.close();
	    } else {
	    	if (this.showRequest) {
				System.out.println();
				System.out.println("Request body is empty");
	    	}
	    }
	    
	    // Wait for response
	    // optionally pretty print 
	    InputStream is = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
	    long endTime = System.nanoTime();
	    
	    // Print response
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()))) {
			String inputLine;
			StringBuffer sb = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				sb.append(inputLine);
			}

			System.out.println();
			System.out.println("Response body:");
			String response = sb.toString();
			
			// Parse and rewrite for pretty printing
			if (mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
				try {
					JsonNode responseNode = mapper.readTree(response);
					System.out.println(mapper.writeValueAsString(responseNode));
				} catch (JsonParseException e) {
					System.out.println(response);
				}
			} else {
				System.out.println(response);
			}
		}
	    
	    System.out.printf("%nTime %d milliseconds%n", (endTime - startTime) / 1000000);
	}


	
}
