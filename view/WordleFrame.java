package edu.wm.cs.cs301.wordle.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import edu.wm.cs.cs301.wordle.model.AppColors;
import edu.wm.cs.cs301.wordle.model.WordleModel;

public class WordleFrame {
	
	private final JFrame frame;
	
	private final KeyboardPanel keyboardPanel;
	
	private final WordleModel model;
	
	private final WordleGridPanel wordleGridPanel;
	
	public JButton hintButton;//
	
	public Timer timer = new Timer(1000, null);
	
	public JLabel timerLabel = new JLabel("Timer");
	
	public int remainingTimeInSeconds = 5 * 60; // 5 minutes in seconds
    
	public WordleFrame(WordleModel model) {
		this.model = model;
		this.keyboardPanel = new KeyboardPanel(this, model);
		int width = keyboardPanel.getPanel().getPreferredSize().width;
		this.wordleGridPanel = new WordleGridPanel(this, model, width);
		this.hintButton = new JButton();//

		this.frame = createAndShowGUI();
	}
	
	private JFrame createAndShowGUI() {
		JFrame frame = new JFrame("Wordle");
		JPanel mainPanel = new JPanel(new BorderLayout());// Create a main panel
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setJMenuBar(createMenuBar());
		frame.setResizable(false);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				shutdown();
			}
		});
		
		//Create hint panel
		JPanel hintPanel = new JPanel();
		hintPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		hintButton = createHintButton();
		hintButton.setFocusable(false);
		hintPanel.add(hintButton);
		
		//Create timer panel
	    timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
	    JPanel timerPanel= new JPanel();
	    timerPanel.add(timerLabel);

	    // Add panels to upper panel
	    JPanel upperPanel = new JPanel(new BorderLayout());
	    upperPanel.add(timerPanel, BorderLayout.NORTH);
	    upperPanel.add(wordleGridPanel, BorderLayout.CENTER);
	    
	    // Add panels to main panel
	    mainPanel.add(upperPanel, BorderLayout.NORTH);
		mainPanel.add(hintPanel, BorderLayout.SOUTH);
		
		// Arrange the panels
		frame.add(createTitlePanel(), BorderLayout.NORTH);
		frame.add(mainPanel, BorderLayout.CENTER);
		frame.add(keyboardPanel.getPanel(), BorderLayout.SOUTH);

		timer = renewTimer(timerLabel);
		timer.start();
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		
		System.out.println("Frame size: " + frame.getSize());
		
		return frame;
	}
	
	public Timer getTimer() {
		return this.timer;
	}
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		//Difficulty
		JMenu difficultyMenu = new JMenu("Difficulty");
		menuBar.add(difficultyMenu);
		
		//Kids
		JMenuItem kidsLevel = new JMenuItem("Kids");
		kidsLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	stopTimer();//
            	model.setDifficulty("Kids");
            	WordleFrame wf = new WordleFrame(model);
            	wf.repaintWordleGridPanel(); //!
            }
        });
		difficultyMenu.add(kidsLevel);
		
		//Normal
		JMenuItem normalLevel = new JMenuItem("Normal");
		normalLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	stopTimer();//
            	model.setDifficulty("Normal");
            	WordleFrame wf2 = new WordleFrame(model);
            	wf2.repaintWordleGridPanel(); //!
            }
        });
		difficultyMenu.add(normalLevel);
		
		//Hard
		JMenuItem hardLevel = new JMenuItem("Hard");
		hardLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	stopTimer();//
            	model.setDifficulty("Hard");
            	WordleFrame wf3 = new WordleFrame(model);
            	wf3.repaintWordleGridPanel(); //!
            }
        });
		difficultyMenu.add(hardLevel);
		
		
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
		
		JMenuItem instructionsItem = new JMenuItem("Instructions...");
		instructionsItem.addActionListener(event -> new InstructionsDialog(this));
		helpMenu.add(instructionsItem);
		
		JMenuItem aboutItem = new JMenuItem("About...");
		aboutItem.addActionListener(event -> new AboutDialog(this));
		helpMenu.add(aboutItem);
		
		return menuBar;
	}
	
	private JPanel createTitlePanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		
		InputMap inputMap = panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelAction");
		ActionMap actionMap = panel.getActionMap();
		actionMap.put("cancelAction", new CancelAction());
		
		JLabel label = new JLabel("Wordle");
		label.setFont(AppFonts.getTitleFont());
		panel.add(label);
		
		return panel;
	}
	
	public void shutdown() {
		model.getStatistics().writeStatistics();
		frame.dispose();
		System.exit(0);
	}
	
	public void resetDefaultColors() {
		keyboardPanel.resetDefaultColors();
	}
	
	public void setColor(String letter, Color backgroundColor, Color foregroundColor) {
		keyboardPanel.setColor(letter, backgroundColor, foregroundColor);
	}
	
	public void repaintWordleGridPanel() {
		wordleGridPanel.repaint();
	}

	public JFrame getFrame() {
		return frame;
	}
	
	//Create hint button
	private JButton createHintButton() {
		JButton hintButton = new JButton("Hint");
		changeHintColor(hintButton);
		
		hintButton.addActionListener((ActionListener) new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				model.getHint();
				changeHintColor(hintButton);
				repaintWordleGridPanel();
			}
		});
		return hintButton;
	}
	
	//change button color
	public void changeHintColor(JButton theHint) {
		if (model.getValidity()==false) {
			theHint.setForeground(AppColors.GRAY);
		}else {
			theHint.setForeground(Color.BLACK);
		}
		theHint.repaint();
	}
	
	
	public Timer renewTimer(JLabel timerLabel) {
	    if (!timer.isRunning()) {
	        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
	        remainingTimeInSeconds = 300;
	        this.timer = new Timer(1000, new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                remainingTimeInSeconds--;
	                if (remainingTimeInSeconds >= 0) {
	                    int minutes = remainingTimeInSeconds / 60;
	                    int seconds = remainingTimeInSeconds % 60;
	                    timerLabel.setText(String.format("Timer: %02d:%02d", minutes, seconds));
	                } else {
	                    stopTimer();
	                    timerLabel.setText("Time's up!");
	                    timesUp();
	                }
	            }
	        });
	        timer.start();
	    }
	    return this.timer;
	}
	
	public void stopTimer() {
		this.timer.stop();
	}
	
	public void timesUp() {
	    repaintWordleGridPanel();
		model.getStatistics().incrementTotalGamesPlayed();
		model.getStatistics().setCurrentStreak(0);
        new StatisticsDialog(this,model);
	}
	
	private class CancelAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent event) {
			shutdown();
		}
		
	}
	
}
