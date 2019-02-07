package auraya.v8.body;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(Include.NON_EMPTY)
public class CrossMatch {
	@JsonProperty("ids")
	private List<String> ids = new ArrayList<String>();

	@JsonProperty("utterance")
	private Utterance utterance = null;

	@JsonProperty("channel")
	private String channel = null;

	@JsonProperty("override")
	private String override = null;
}
