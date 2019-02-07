package auraya;


import lombok.Getter;

public enum SupportedApi {
	
	CHECK_HEALTH("ch"),
	CHECK_ENROLLED("ce"), 
	CHECK_QUALITY("cq"), 
	
	ENROL("e"), 
	VERIFY("v"), 
	DELETE("d"), 
	CROSS_MATCH("cm"), 
	DETECT_GENDER("dg"), 
	
	GET_PHRASE("gp"), 
	GET_VOICEPRINT("gvp"),
	
	// Less commonly used APIs
	CHECK_SIMILARITY("cs"),
	RANK_MODEL("rm");
	
	@Getter private final String acronym;
	
	private SupportedApi(String acronym) {
		this.acronym = acronym;
	}
	
	public static SupportedApi create(String name) {
        for (SupportedApi api : SupportedApi.values()) {
        	if (api.getAcronym().equalsIgnoreCase(name) || api.toString().equalsIgnoreCase(name)) return api;
        }
        return null;
	}
	
	
}
