import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        String window = "";
        char c;
        In in = new In(fileName);
        int windowLengthIndex = 0;

        while (!in.isEmpty() && windowLengthIndex < windowLength) {
            c = in.readChar();

            window += c;
            windowLengthIndex++;
        }

        while (!in.isEmpty()) {

            c = in.readChar();

            List probs = CharDataMap.get(window);

            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }
        
            probs.update(c);

            window = window.substring(1) + c;
            // System.out.println(window);
        }
        
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }
        
        

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		int countSum = 0;
        ListIterator itr1 = probs.listIterator(0);
        CharData prev;

        while (itr1.hasNext()) {
            countSum = countSum + itr1.next().count;
        }

        ListIterator itr2 = probs.listIterator(1);

        CharData first = probs.getFirst();
        first.p = (double) first.count / countSum;
        first.cp = first.p;
        prev = first;

        while (itr2.hasNext()) {
            CharData charData = itr2.next();
            charData.p = (double) charData.count / countSum;
            charData.cp = prev.cp + charData.p;
            prev = charData;
        }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();
        CharData charData;

        ListIterator itr = probs.listIterator(0);

        while (itr.hasNext()) {
            charData = itr.next();
            if (charData.cp >= r) {
                return charData.chr;
            }
        }

        return probs.get(probs.getSize() - 1).chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength) return initialText;

        String generatedText = initialText;

        String window = generatedText.substring(generatedText.length() - windowLength);

        while (generatedText.length() < textLength + 7) {
            List currentWindowList = CharDataMap.get(window);

            if (currentWindowList == null) return generatedText;
            
            generatedText += getRandomChar(currentWindowList);

            window = generatedText.substring(generatedText.length() - windowLength);
        }

        return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
        lm = new LanguageModel(windowLength);
        else
        lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
    }    
}
