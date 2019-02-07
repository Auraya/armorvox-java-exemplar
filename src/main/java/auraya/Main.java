package auraya;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import auraya.ArmorvoxClient.UtteranceParameters;
import lombok.SneakyThrows;

/**
 * Main entry point for the example Armorvox client application. 
 * Marshals command line options for use by the "ArmorvoxClient" class.
 * 
 * The command line accepts multiple utterances and corresponding utterance options:
 *    phrase
 *    check_quality
 *    recognition
 *    vocab
 *    
 * If these corresponding options are omitted then the defaults are used for all utterances.
 * If these corresponding options specified just once, then they apply to all utterances.
 * If these corresponding options specified just multiple times, 
 * 		then they apply to utterances in the order they appear on command line, 
 * 		cycling through the order if there are fewer.
 * 
 * An utterance option can be 'commented out' by changing it to -ux /path/to/utterance
 * 
 * All paths are either absolute or relative to current working directory.
 * 
 * 
 * @author Jamie Lister
 *
 */
public class Main {

	public static void main(String[] args) {

		// Set up Command Line Interface options
		Options options = new Options();
		
		// Server options
		options.addOption("s", 	"server", true, "scheme, address or name of Armorvox server and port. Default is 'http://localhost:9005/v8'");
		options.addOption("g", 	"group", true, "Group name to use. Default is 'my_group'");
		options.addOption("a", 	"api", true, "API to use. Can be acronym e.g, 'ce' for 'check_enrol'. Default is 'enrol'. Supported APIs: " + StringUtils.join(Arrays.asList(SupportedApi.values()).stream().map(a -> { return a.toString().toLowerCase(); }).collect(Collectors.toList()), ", "));
		
		
		// Print selection options
		options.addOption("pn", "print_name", true, "print name, default is 'digit'");
		options.addOption("i", 	"id", true, "The ID(s) to enrol, verify, delete, or cross_match. Also used to name models in model_rank API.");
		
		// Utterance options
		options.addOption("u", 	"utterance", true, "The utterance(s) to use. Grouped with corresponding p, cq, r, vc options.");
		options.addOption("p", "phrase", true, "Utterance(s) are checked with provided text-prompted string, or if 'file' then contents of adjacent file with extension .txt");
		options.addOption("cq", "check_quality", true, "Utterance(s) quality check will be checked. Default is 'true'");
		options.addOption("r", 	"recognition", true, "Utterance(s) phrase recognition on the provided utterances. Default is 'false'");
		options.addOption("vc", "vocab", true, "Utterance(s) vocab used by text prompted. Default is 'en_digits'");
		
		// Miscellaneous options
		options.addOption("m", 	"mode", true, "Mode used by check_quality API. Accepts 'enrol', 'verify', 'cross_match' and 'characterise'. Default is 'enrol'");
		options.addOption("o", 	"override", true, "Overrides configuration parameter(s) in the request");
		options.addOption("ch", "channel", true, "Sets the channel request parameter. Default is null (not specified)");
		
		// Output options
		options.addOption("sr", "show_request", false, "Show the JSON request object if specified");
		options.addOption("pp", "print_print", false, "Pretty print the JSON request and response if specified");
		options.addOption("xu", "exclude_utterance", false, "Simple way to comment out an utterance from command line"); // This is used to make editing the command line easier
		
		options.addOption("h", "help", true, "Prints help message");


		try {
				
			CommandLine cli = new DefaultParser().parse(options, args);
			
			// Determine API
			if (cli.hasOption('h')) throw new ParseException(""); // Show HELP
			
			SupportedApi api = SupportedApi.create(cli.getOptionValue('a', "e"));
			if (api == null) throw new ParseException("API not supported: " + cli.getOptionValue('a', "e"));
			
			// Set up API parameters
			String[] uttPaths = cli.getOptionValues('u');
			String[] vocabs = cli.getOptionValues("vc");
			String[] phrases = cli.getOptionValues("p");
			String[] isRecognitionArray = cli.getOptionValues('r');
			String[] checkQualityArray = cli.getOptionValues("cq");
			UtteranceParameters[] utts = makeUtteranceParametersArray(uttPaths, checkQualityArray, phrases, vocabs, isRecognitionArray);
			
			String server = cli.getOptionValue('s', "http://localhost:9005/v8");
			String group = cli.getOptionValue('g', "my_group");
			String printName = cli.getOptionValue("pn", "digit");
			String[] ids = cli.getOptionValues('i');
			
			String overrides = StringUtils.join(cli.getOptionValues('o'), "\n");
			String channel = cli.getOptionValue("ch", null);
			String mode = cli.getOptionValue("m", null);
			
			boolean prettyPrint = cli.hasOption("pp");
			boolean showRequest = cli.hasOption("sr");
			
			ArmorvoxClient client = new ArmorvoxClient(server, group, prettyPrint, showRequest);
			
			// Check API has required options set. Call client.
			switch (api) {
			case ENROL: 
				if (ids == null || ids.length != 1) throw new ParseException("Enrol API must have 1 id");
				if (utts == null) throw new ParseException("Enrol API must have utterance(s)");
				client.enrol(ids[0], printName, utts, channel, overrides);
				break;
				
			case VERIFY: 
				if (ids == null || ids.length != 1) throw new ParseException("Verify API must have 1 id");
				if (utts == null || utts.length != 1) throw new ParseException("Verify API must have 1 utterance");
				client.verify(ids[0], printName, utts[0], channel, overrides);
				break;
				
			case GET_VOICEPRINT:
				if (ids == null || ids.length != 1) throw new ParseException("Get Voiceprint API must have 1 id");
				client.getVoicePrint(ids[0], printName, false);
				break;
				
			case CHECK_ENROLLED:
				if (ids == null || ids.length != 1) throw new ParseException("Check Enrolled API must have 1 id");
				client.getVoicePrint(ids[0], printName, true);
				break;
				
			case CHECK_HEALTH: 
				client.checkHealth();
				break;
			
			case CHECK_QUALITY:
				if (utts == null || utts.length != 1) throw new ParseException("Check Quality API must have 1 utterance");
				client.checkQuality(printName, utts[0], mode, channel, overrides);
				break;
				
			case CROSS_MATCH:
				if (ids == null) throw new ParseException("Cross Match API must have id(s)");
				if (utts == null || utts.length != 1) throw new ParseException("Cross Match API must have 1 utterance");
				client.crossMatch(ids, printName, utts[0], channel, overrides);
				break;
				
			case DELETE:
				if (ids == null || ids.length != 1) throw new ParseException("Delete API must have 1 id");
				client.delete(ids[0], printName);
				break;
				
			case DETECT_GENDER:
				if (utts == null) throw new ParseException("Verify API must have utterance(s)");
				client.detectGender(utts, overrides);
				break;
				
			case GET_PHRASE:
				String vocab = vocabs != null?vocabs[0]:"en_digits";
				client.getPhrase(vocab);
				break;
				
			case CHECK_SIMILARITY:
				if (utts == null || utts.length != 2) throw new ParseException("Check similarity API must have 2 utterances");
				client.similariy(utts, overrides);
				break;
				
			case RANK_MODEL:
				if (utts == null) throw new ParseException("Model Rank API must have utterance(s)");
				client.modelRank(utts, ids, overrides);
			}
			
		} catch (ParseException pe) {
			
			// Print HELP message
			HelpFormatter formatter = new HelpFormatter();
			formatter.setOptionComparator(null);
			formatter.printHelp("java -jar armorvox-client.jar ", options, true);
			System.out.println();
			System.out.println(pe.getMessage());
		} catch (Exception e) {
			
			// Some other problem
			e.printStackTrace();
		}
	}

	/**
	 * Bundle utterance parameters into a single class
	 * 
	 * @param utts
	 * @param checkQualityArray
	 * @param phrases
	 * @param vocabs
	 * @param isRecognitionArray
	 * @return
	 */
	@SneakyThrows
	private static UtteranceParameters[] makeUtteranceParametersArray(String[] utts, String[] checkQualityArray, String[] phrases, String[] vocabs, String[] isRecognitionArray) {
		UtteranceParameters[] result = new UtteranceParameters[utts.length];
		for (int i = 0; i < utts.length; i++) {
			String uttPath = utts[i];
			String phrase = getString(phrases, i);
			if (phrase != null) {
				if (phrase.equals("file")) {
					String textPath = uttPath.replaceFirst("\\.wav$", ".txt");
					byte[] textBytes = Files.readAllBytes(Paths.get(textPath));		
					phrase = new String(textBytes, Charset.defaultCharset()).trim();
				}
			}
			
			result[i] = new UtteranceParameters(uttPath, getBoolean(checkQualityArray, i), phrase, getString(vocabs, i), getBoolean(isRecognitionArray, i));
		}
		return result;
	}
	
	private static Boolean getBoolean(String[] array, int index) {
		if (array == null || array.length == 0) return null;
		return Boolean.valueOf(array[index % array.length]);
	}
	
	private static String getString(String[] array, int index) {
		if (array == null || array.length == 0) return null;
		return array[index % array.length];
	}
}
