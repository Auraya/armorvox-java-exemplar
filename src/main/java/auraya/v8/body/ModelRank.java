package auraya.v8.body;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_EMPTY)
public class ModelRank {
	@JsonProperty("utterances")
	private List<Utterance> utterances = new ArrayList<>();

	@JsonProperty("ubm_names")
	private List<String> ubmNames = new ArrayList<>();
	
	@JsonProperty("override")
	private String override = null;
}
