package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {
		
		HashMap<String,Occurrence> result = new HashMap<String,Occurrence>(1000,2.0f);
		Scanner sc = null;
		
		try {
			sc = new Scanner(new File(docFile));
			
		} catch (FileNotFoundException f) {
			f.printStackTrace();
		}
		
		while(sc.hasNext()) {
			
			String word=sc.next();
			String keyword = getKeyword(word);
			
			if(keyword!=null) {
				
				if(result.containsKey(keyword)==false) {
					Occurrence ocr = new Occurrence(docFile,1);
					result.put(keyword, ocr);
				}
				else {
					Occurrence ocr=result.get(keyword);
					ocr.frequency++;
					result.put(keyword, ocr);
				}		
			}	
		}
		
		sc.close();
		
		return result;
	}

	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		
		for(String key:kws.keySet()) {
			
			ArrayList<Occurrence> temp = new ArrayList<Occurrence>();
			
			if(!keywordsIndex.containsKey(key)) {
				temp.add(kws.get(key));
				insertLastOccurrence(temp);
				keywordsIndex.put(key, temp);
			}
			else {
				temp = keywordsIndex.get(key);
				temp.add(kws.get(key));
				insertLastOccurrence(temp);
				keywordsIndex.put(key,temp);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		
		String result = "";
		
		if(word == null || word.equals(null)) {
			return null;
		}
		
		for (int c=word.length()-1;c>=0;c--) {
			if(!Character.isLetter(word.charAt(c))) {
				word.substring(0,word.length()-1);
			}
			else {
				break;
			}
		}
		
		for (int i=0; i<word.length();i++) {
			if(Character.isLetter(word.charAt(i))) {
				char ch = Character.toLowerCase(word.charAt(i));
				result += ch;
			}
			else {
					return null;		
				}
			}
		
		if(noiseWords.contains(result)) {
			return null;
		}
		
		else {
			
			if(result.length() <=0) {
				return null;
			}
			
			return result;
		}
		
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		/** COMPLETE THIS METHOD **/
		
		if(occs.size()==1 || occs==null) {
			return null;
		}
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		Occurrence ocr = occs.get(occs.size()-1);
		occs.remove(occs.size()-1);
		
		int high=0;
		int low=occs.size()-1;
		int mid=0;
		int midFreq;
		
		while(high<=low) {
			
			mid =(high+low)/2;
			midFreq = occs.get(mid).frequency;
			
			if(midFreq==ocr.frequency) {
				result.add(mid);
				break;
			}
			else if(midFreq>ocr.frequency) {
				high = mid+1;
				result.add(mid);
				if(high <=mid) {
					mid++;
				}
			}
			else {
				low = mid-1;
				result.add(mid);
			}
		}
		
		occs.add(mid, ocr);
		
		return result;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		
		ArrayList<String> top5 = new ArrayList<String>();
		
		kw1 = kw1.toLowerCase();
		kw2 = kw2.toLowerCase();
		
		if(!keywordsIndex.containsKey(kw2) && !keywordsIndex.containsKey(kw1)) {
			return null;
		}
		
		if(kw2 == null && kw1==null) {
			return null;
		}
		
		if(keywordsIndex.containsKey(kw1) && !keywordsIndex.containsKey(kw2)) {
			ArrayList<Occurrence> ocrkw1 = keywordsIndex.get(kw1);
				for(int i=0; i<=ocrkw1.size()-1;i++) {
					top5.add(ocrkw1.get(i).document);
					if(top5.size()==5) {
						break;
					}	
				}
				return top5;
			}
		
		if(keywordsIndex.containsKey(kw2) && !keywordsIndex.containsKey(kw1)) {
			ArrayList<Occurrence> ocrkw2 = keywordsIndex.get(kw2);
				for(int i=0; i<=ocrkw2.size()-1;i++) {
					top5.add(ocrkw2.get(i).document);
					if(top5.size()==5) {
						break;
					}	
				}
				return top5;
			}	
		
		if(keywordsIndex.containsKey(kw1) && keywordsIndex.containsKey(kw2)) {
		
			ArrayList<Occurrence> ocrkw2 = keywordsIndex.get(kw2);
			ArrayList<Occurrence> ocrkw1 = keywordsIndex.get(kw1);
			
			int i=0;
			
			int c=0;
			
			for(; c<ocrkw1.size();) {
				
				if(i==ocrkw2.size()) {
					i--;
				}
				
				if(top5.size()==5) {
					break;
				}
				
				if(ocrkw1.get(c).frequency == ocrkw2.get(i).frequency) {
					if(ocrkw1.get(c).document==ocrkw2.get(i).document) {
						if(top5.contains(ocrkw1.get(c).document)) {
							c++;
							i++;
						}
						else {
							top5.add(ocrkw1.get(c).document);
							c++;	
							i++;
						}
					}
					else {
						
					if(top5.contains(ocrkw1.get(c).document) && top5.contains(ocrkw2.get(i).document)) {
						c++;
						i++;
					}
					else if(top5.contains(ocrkw1.get(c).document)) {
						top5.add(ocrkw2.get(i).document);
						c++;
						i++;
					}
					else if(top5.contains(ocrkw2.get(i).document)) {
						top5.add(ocrkw1.get(c).document);
						c++;	
						i++;
					}
					else {
						top5.add(ocrkw1.get(c).document);
						top5.add(ocrkw2.get(i).document);
						c++;	
						i++;
					}
				}
			}
				else if(ocrkw1.get(c).frequency > ocrkw2.get(i).frequency) {
					if(top5.contains(ocrkw1.get(c).document)) {
						c++;
					}
					else {
						top5.add(ocrkw1.get(c).document);
						c++;
					}
				}
				else if(ocrkw1.get(c).frequency < ocrkw2.get(i).frequency) {
					if(top5.contains(ocrkw2.get(i).document)) {
						i++;
						if(i==ocrkw2.size()) {
							top5.add(ocrkw1.get(c).document);
							c++;
						}
					}
					else {
						top5.add(ocrkw2.get(i).document);
						i++;
					}
				}
			}
			
			for(; i<ocrkw2.size();) {
				
				if(c==ocrkw1.size()) {
					c--;
				}
				
				if(top5.size()==5) {
					break;
				}
				
				if(ocrkw1.get(c).frequency == ocrkw2.get(i).frequency) {
					if(ocrkw1.get(c).document==ocrkw2.get(i).document) {
						if(top5.contains(ocrkw1.get(c).document)) {
							c++;
							i++;
						}
						else {
							top5.add(ocrkw1.get(c).document);
							c++;	
							i++;
						}
					}
					else {
						if(top5.contains(ocrkw1.get(c).document) && top5.contains(ocrkw2.get(i).document)) {
							c++;
							i++;
						}
						else if(top5.contains(ocrkw1.get(c).document)) {
							top5.add(ocrkw2.get(i).document);
							c++;
							i++;
						}
						else if(top5.contains(ocrkw2.get(i).document)){
							top5.add(ocrkw1.get(c).document);
							c++;	
							i++;
						}
						else {
							top5.add(ocrkw1.get(c).document);
							top5.add(ocrkw2.get(i).document);
							c++;	
							i++;
						}	
					}
				}
				else if(ocrkw1.get(c).frequency > ocrkw2.get(i).frequency) {
					if(top5.contains(ocrkw1.get(c).document)) {
						c++;
						if(c==ocrkw1.size()) {
							top5.add(ocrkw2.get(i).document);
							i++;
						}
					}
					else {
						top5.add(ocrkw1.get(c).document);
						c++;
					}
				}
				else if(ocrkw1.get(c).frequency < ocrkw2.get(i).frequency) {
					if(top5.contains(ocrkw2.get(i).document)) {
						i++;
						if(c==ocrkw1.size()) {
							top5.add(ocrkw2.get(i).document);
							i++;
						}
					}
					else {
						top5.add(ocrkw2.get(i).document);
						i++;
					}
				}
			}
		}
		return top5;
	}

}		
			
			