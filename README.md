# CheckMateFilter
Spellchecking and profanity filtering library

## Getting started
Simply clone the project and build the jar with maven. Then you can use it in your project as a external library or run it by executing command `java -jar checkmate-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## Performance
* The performance of a spelling check doesn't strongly depend on dictionary size, it just changes on 2-5 ms, but is sensitive to the length of a given word. Also, the number of words that is close by match percentage.

|Dictionary size|Operation|Match percentage|Time (milliseconds)|Results|
|----|----|----|----|----|
|80k+|Checking word 'catn'|70%|25.76 ms|[cant, can, cats]|
|80k+|Checking word 'catn'|60%|123.47 ms|[cant, can, cats]|
|80k+|Checking word 'belive'|70%|18.29 ms|[believe, belize, belie]|
|80k+|Checking word 'belive'|60%|23.24 ms|[believe, belize, belie]|
|10k+|Checking word 'catn'|70%|21.16 ms|[can, cats, cat]|
|10k+|Checking word 'catn'|60%|23.16 ms|[can, cats, cat]|
|10k+|Checking word 'belive'|70%|22.19 ms|[believe, belize, believes]|
|10k+|Checking word 'belive'|60%|60.42 ms|[believe, belize, believes]|

* Profanity filter is much faster, but it also depends on the length of a given string.

|Operation|Time (milliseconds)|Results|
|----|----|----|
|Check 'little piece of shit'|15.89 ms|little [profanity]|
|Check 'hello, cunt'|1.08 ms|hello, [profanity]|
|Check 'scum'|0.09 ms|[profanity]|

* And lastly, text preproccessor. You have to take into account repeated letter removal and number of words to check.

|Operation|Time (milliseconds)|Repeated letters|Results|
|----|----|----|----|
|Preproccess 'little pi3ce @nd sh1t'|6.80ms|false|little piece and shit|
|Preproccess 'little pi3ceeee @nd sh1t'|10.80ms|true|litle piece and shit|
|Preproccess 'shi+'|5.74ms|true|shit|

## Usage

You should use TextFilter class for all text manipulations. it can do next operations:
 
* List<String> checkWord(String word)
* List<String> checkCompound(String compound)
* String checkText(String text) 
* String censor(String text) 
* Censored searchForProfanity(String text) 
* String preproccess(String text, boolean removeRepeatedLetters) 
* boolean isValid(String word) 
* boolean isProfane(String word)
  
 Sample usage:
  
``` java

public class Demo {
	public static void main(String[] args) {

  TextFilter textFilter = new TextFilter(Language.ENGLISH);

        String word = textFilter.checkWord("cutn").get(0);
        System.out.println(textFilter.censor(word));
        textFilter.setProfanityReplacement("[****]");
        System.out.println(textFilter.searchForProfanity("bitch"));

        String text = textFilter.preproccess("p13ce of sh1t", false);
        System.out.println(textFilter.searchForProfanity(text));

        String compound = textFilter.checkCompound("holyshit").get(0);
        System.out.println(textFilter.searchForProfanity(compound));
  }
}
       
```
       
And you can tune its work by changing these parameters:

* `MaxMatchPercentage` - percent of the length of the given word to match suggestions (default = 70%).
* `Remove repeated letters` - reomve repeated letters in preproccessing (default = false).
* `suggestionLimit` - maximum number of suggestions for given word (default = 5).
* `doPreproccesing` - do preproccessing before spellchecking (default = true).
* `doCheckCompounds` - check for missplelled compounds (default = false).
* `profanityReplacement` - what would be added instead of bad word (default = [censored]).
* `removeProfaneWord` - rmeove or leave a profane word (default = false).

## License

This project is licensed under the GNU GPL-3.0 License - see the `LICENSE.md` file for details

## Acknowledgments

* Lots of thanks to anyone who's code was used
