package auraya.v8.body;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class Utterance {	
	@JsonProperty("content")
	private byte[] content = null;

	@JsonProperty("phrase")
	private String phrase = null;
	
	@JsonProperty("vocab")
	private String vocab = null;
	
	@JsonProperty("feature_vector")
	private Boolean featureVector = null;
	
	@JsonProperty("check_quality")
	private Boolean check_quality = null;
	
	@JsonProperty("recognition")
	private Boolean recognition = null;
}
