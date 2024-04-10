package edu.wm.cs.cs301.wordle.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.wm.cs.cs301.wordle.controller.ReadWordsRunnable;

public class WordleModel {
	
	private char[] currentWord, guess;
	
	private int columnCount, maximumRows; //final
	private int currentColumn, currentRow;
	
	private List<String> wordList;
	
	private final Random random;
	
	private final Statistics statistics;
	
	private WordleResponse[][] wordleGrid;
	
	private String difficulty = "Normal";
	
	public WordleModel() {
		this.currentColumn = -1;
		this.currentRow = 0;
		this.columnCount = 5;
		this.maximumRows = 6;
		this.random = new Random();
		
		createWordList();
		
		this.wordleGrid = initializeWordleGrid();
		this.guess = new char[columnCount];
		this.statistics = new Statistics(difficulty);
	}
	
	private void createWordList() {
		ReadWordsRunnable runnable = new ReadWordsRunnable(this);
		Thread wordListThread = new Thread(runnable);
		wordListThread.start();
		
		try {
			wordListThread.join();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	public void initialize() {
		this.wordleGrid = initializeWordleGrid();
		this.currentColumn = -1;
		this.currentRow = 0;
		
		generateCurrentWord();
		this.guess = new char[columnCount];
	}

	public void generateCurrentWord() {
		String word = wordList.get(getRandomIndex());
		this.currentWord = word.toUpperCase().toCharArray();
		
		System.out.println("DEBUG: Current word set to " + word);
	}

	public String getCurrentWord() {
		//this.currentWord.toString();
		return String.valueOf(this.currentWord);
	}
	
	//
	public void setCurrentWord(char[] newCurrentWord) {
		this.currentWord = newCurrentWord;
	}
	
	//
	public char[] getCurrentGuess() {
		return this.guess;
	}
	
	// Assist Testing
	public void setCurrentGuess(char[] newGuess) {
		this.guess = newGuess;
	}

	private int getRandomIndex() {
		int size = wordList.size();
		return random.nextInt(size);
	}
	
	private WordleResponse[][] initializeWordleGrid() {
		WordleResponse[][] wordleGrid = new WordleResponse[maximumRows][columnCount];

		for (int row = 0; row < wordleGrid.length; row++) {
			for (int column = 0; column < wordleGrid[row].length; column++) {
				wordleGrid[row][column] = null;
			}
		}

		return wordleGrid;
	}
	
	public void setWordList(List<String> wordList) {
		this.wordList = wordList;
	}
	
	public void setCurrentColumn(char c) {
		currentColumn++;
		currentColumn = Math.min(currentColumn, (columnCount - 1));
		guess[currentColumn] = c;
		wordleGrid[currentRow][currentColumn] = new WordleResponse(c,
				Color.WHITE, Color.BLACK);
	}
	
	public void backspace() {
		if (this.currentColumn > -1) { //only backspace if there's room
			wordleGrid[currentRow][currentColumn] = null;
			guess[currentColumn] = ' ';
			this.currentColumn--;
			this.currentColumn = Math.max(currentColumn, -1);
		}
	}
	
	public WordleResponse[] getCurrentRow() {
		return wordleGrid[getCurrentRowNumber()];
	}
	
	public int getCurrentRowNumber() {
		return currentRow - 1;
	}
	
	public boolean setCurrentRow() {		
		for (int column = 0; column < guess.length; column++) {
			Color backgroundColor = AppColors.GRAY;
			Color foregroundColor = Color.WHITE;
			if (guess[column] == currentWord[column]) {
				backgroundColor = AppColors.GREEN;
			} else if (contains(currentWord, guess, column)) {
				backgroundColor = AppColors.YELLOW;
			}
			
			wordleGrid[currentRow][column] = new WordleResponse(guess[column],
					backgroundColor, foregroundColor);
		}
		
		currentColumn = -1;
		currentRow++;
		guess = new char[columnCount];
		
		return currentRow < maximumRows;
	}
	
	private boolean contains(char[] currentWord, char[] guess, int column) {
		for (int index = 0; index < currentWord.length; index++) {
			if (index != column && guess[column] == currentWord[index]) {
				return true;
			}
		}
		
		return false;
	}

	public WordleResponse[][] getWordleGrid() {
		return wordleGrid;
	}
	
	public int getMaximumRows() {
		return maximumRows;
	}

	public int getColumnCount() {
		return columnCount;
	}
	
	public void setDifficulty(String level) {
		difficulty = level;
		if (level.equals("Kids")){
			this.columnCount = 3;
			this.maximumRows = 4;
		}else if (level.equals("Normal")){
			columnCount = 5;
			maximumRows = 6;
		}else if (level.equals("Hard")){
			columnCount = 7;
			maximumRows = 8;
		}
		createWordList();
		initialize();
	}
	
	public int getCurrentColumn() {
		return currentColumn;
	}

	public int getTotalWordCount() {
		return wordList.size();
	}

	public Statistics getStatistics() {
		return statistics;
	}
	
	// Get last guess in the previous row
	public char[] getLastGuess() {
		char[] lastGuess = new char[columnCount];
		for (int column = 0; column < columnCount; column++) {
			lastGuess[column] =  wordleGrid[currentRow-1][column].getChar();
		}
		return lastGuess;
	}
	
	// Get Hint Validity
	public boolean getValidity() {
		if (currentRow != 0){
			char[] lastGuess = getLastGuess();
			for (int column = 0; column < columnCount; column++) {
				if (!contains(currentWord, lastGuess, column) && currentWord[column]!=lastGuess[column]) {
					return true;
				}
			}
		}
		return false;
	}
	
	// Get hint and implement
	public void getHint() {
		if (currentRow != 0){
			char[] lastGuess = getLastGuess();
			ArrayList <Integer> possibleHint = new ArrayList<Integer>(); // create a list to store the columns of letters that can be hinted 
			for (int column = 0; column < columnCount; column++) {
				if (!contains(currentWord, lastGuess, column) && currentWord[column]!=lastGuess[column]) {
					possibleHint.add(column);
				}
			}
			int hintCol = possibleHint.get((int) (Math.random()* possibleHint.size()));
			wordleGrid[currentRow-1][hintCol] = new WordleResponse(currentWord[hintCol], AppColors.GREEN, Color.WHITE);
		}
	}
	
}
