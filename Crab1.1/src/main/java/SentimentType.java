import java.util.NoSuchElementException;

/**
 * An enum for the Simple CoreNLP API to represent a sentiment value.
 *
 * @author <a href="mailto:angeli@stanford.edu">Gabor Angeli</a>
 */
public enum SentimentType {
    VERY_POSITIVE,
    POSITIVE,
    NEUTRAL,
    NEGATIVE,
    VERY_NEGATIVE,
    ;

    /**
     * Get the sentiment class from the Stanford Sentiment Treebank
     * integer encoding. That is, an integer between 0 and 4 (inclusive)
     *
     * @param sentiment The Integer representation of a sentiment.
     *
     * @return The sentiment class associated with that integer.
     */
    public static SentimentType fromInt(final int sentiment) {
        return switch (sentiment) {
            case 0 -> VERY_NEGATIVE;
            case 1 -> NEGATIVE;
            case 2 -> NEUTRAL;
            case 3 -> POSITIVE;
            case 4 -> VERY_POSITIVE;
            default -> throw new NoSuchElementException("No sentiment value for integer: " + sentiment);
        };
    }
}