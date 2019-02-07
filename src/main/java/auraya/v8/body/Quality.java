package auraya.v8.body;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_EMPTY)
public class Quality {
	@JsonProperty("utterance")
	private Utterance utterance = null;

	@JsonProperty("mode")
	private String mode = null;

	@JsonProperty("channel")
	private String channel = null;
	
	@JsonProperty("override")
	private String override = null;
}
