import com.sun.tools.javac.comp.Resolve;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.nio.file.Path;
import java.text.DateFormatSymbols;
import java.util.*;

/**
 * Ticketing System used by the Prom Project.
 * @see Prom
 * @see Student
 * @see Table
 * @author Daksh & Matthew
 */
public class TicketingSystem extends JPanel {
    private ArrayList<Student> students;
    private ArrayList<Table> tables;
    private LoginPanel loginPanel;
    private TicketPanel ticketPanel = new TicketPanel(null);
    private FloorPlanPanel floorPanel;
    private FloorPlanSystem floorPlan;
    private File loginCredentials;

    // change
    private final int WINDOW_WIDTH = (int) getToolkit().getScreenSize().getWidth();
    private final int WINDOW_HEIGHT = (int) getToolkit().getScreenSize().getWidth();

    /**
     * Constructs the TicketingSystem. Sets displayed panel to login.
     * @param students The master list of students.
     * @param tables The master list of tables.
     */
    public TicketingSystem(ArrayList<Student> students, ArrayList<Table> tables) {
        this.setLayout(new GridLayout());
        this.students = students;
        this.tables = tables;

        loginCredentials = new File("loginCredentials.txt");

        try {
            initializeStudents();
        } catch (Exception e) {
            e.printStackTrace();
        }

        loginPanel = new LoginPanel();
        this.add(loginPanel);

        this.setVisible(true);
    }

    /**
     * Sets the displayed panel to the ticket panel when the student is logged in.
     */
    private void showTicket() {
        this.removeAll();
        this.add(this.ticketPanel);
        this.ticketPanel.setVisible(true);
        this.ticketPanel.requestFocus();
        this.updateUI();
    }

    /**
     * Sets the displayed panel to the login panel when the student logs off.
     */
    private void showLogin() {
        this.removeAll();
        this.loginPanel = new LoginPanel(); //reset the login panel
        this.add(this.loginPanel);
        this.loginPanel.setVisible(true);
        this.loginPanel.requestFocus();
        this.updateUI();
    }

    /**
     * Sets the displayed panel to the floor plan panel.
     * @param fromPanel the panel from which the panel was called from so it can shift back.
     */
    private void showFloor(JPanel fromPanel){
        floorPanel = new FloorPlanPanel(fromPanel);
        this.removeAll();
        this.add(this.floorPanel);
        this.floorPanel.setVisible(true);
        this.floorPanel.requestFocus();
        this.updateUI();
    }

    /**
     * Adds a student to the master list of students.
     * @param student the student.
     */
    private void addStudent(Student student) {
        this.students.add(student);
    }

    /**
     * Removes a student from the master list of students. Never used, but here for UML.
     * @param student the student.
     */
    private void removeStudent(Student student) {
        this.students.remove(student);
    }

    /**
     * Finds a student based off of ID. Used by login panel.
     * @param id The id.
     * @return The student with the id. Null if not found.
     */
    private Student findStudentByID(String id) {
        for (Student student : students){
            if (student.getId().equals(id)){
                return student;
            }
        }
        return null;
    }

    /**
     * Loads all students from file and recreates the master list
     */
    private void initializeStudents() {
        try {
            Scanner input = new Scanner(loginCredentials);
            ArrayList<String[]> partners = new ArrayList<String[]>();
            ArrayList<String[]> blackList = new ArrayList<String[]>();

            while (input.hasNext()) {
                String student = input.nextLine();
                if (!student.equals("")) {
                    String[] ids = student.split(",");
                    HashMap<String, String> keys = new HashMap<String, String>();
                    for (String key : ids) {
                        String[] temp = key.split(":");
                        if (temp.length > 1){
                        keys.put(temp[0].replace("\"", ""), temp[1].replace("\"", ""));
                        } else {
                            keys.put(temp[0].replace("\"", ""), "");
                        }
                    }

                    String name = keys.get("name");
                    String id = keys.get("id");

                    String[] partnerString = keys.get("partners").split("#");
                    String[] blackListString = keys.get("blacklist").split("#");

                    Boolean hasPaid = Boolean.parseBoolean(keys.get("paid"));
                    String password = keys.get("password");
                    String grade = keys.get("grade");

                    String image = keys.get("image");

                    ArrayList<String> accommodations = new ArrayList<String>();
                    if (keys.get("accommodation").length() > 0) {
                        accommodations.addAll(Arrays.asList(keys.get("accommodation").split("#")));
                    }

                    Student s = new Student(name, id, grade, password);
                    s.setPaid(hasPaid);
                    s.setAccommodations(accommodations);
                    
                    addStudent(s);

                    partners.add(partnerString);
                    blackList.add(blackListString);

                    if (!image.equals("null")){
                        BufferedImage studentImage = ImageIO.read(new File("studentImages/" + id + ".png"));
                        s.setPicture(studentImage);
                    }
                }
            }
            input.close();

            for (int i = 0; i < students.size(); i++) {
                ArrayList<Student> studentPartners = students.get(i).getPartners();
                ArrayList<Student> blackListed = students.get(i).getBlacklist();
                for (int j = 0; j < partners.get(i).length; j++) {
                    Student s = findStudentByID(partners.get(i)[j]);
                    if (s != null) {
                        studentPartners.add(s);
                    }
                }
                for (int j = 0; j < blackList.get(i).length; j++) {
                    Student s = findStudentByID(blackList.get(i)[j]);
                    if (s != null) {
                        blackListed.add(s);
                    }
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Writes all students to file to save all student information.
     */
    private void writeStudents() {
        try {
            FileWriter studentRecords = new FileWriter(loginCredentials, false);
            PrintWriter output = new PrintWriter(studentRecords);

            for (Student curStudent : students) {
                output.print("name:" + curStudent.getName() + ",");
                output.print("id:" + curStudent.getId() + ",");
                output.print("paid:" + curStudent.hasPaid() + ",");
                output.print("grade:" + curStudent.getGrade() + ",");
                output.print("password:" + curStudent.getPassword() + ",");
                if (curStudent.getPicture() != null) {
                    ImageIO.write(curStudent.getPicture(), "png", new File("studentImages/" + curStudent.getId() + ".png"));
                    output.print("image:" + curStudent.getId() + ".png,");
                } else {
                    output.print("image:null,");
                }
                String partnerString = "";
                ArrayList<Student> partnerArray = curStudent.getPartners();
                for (int i = 0; i < partnerArray.size(); i++) {
                    partnerString += partnerArray.get(i).getId() + "#";
                }
                if (partnerString.length() != 0) {
                    partnerString = partnerString.substring(0, partnerString.length() - 1);
                }

                String accommodationString = "";
                ArrayList<String> accommodationArray = curStudent.getAccommodations();
                for (int i = 0; i < accommodationArray.size(); i++){
                    accommodationString += accommodationArray.get(i) + "#";
                }
                if (accommodationString.length() != 0){
                    accommodationString = accommodationString.substring(0, accommodationString.length() - 1);
                }

                String blackListString = "";
                ArrayList<Student> blackListArray = curStudent.getBlacklist();
                for (int i = 0; i < blackListArray.size(); i++) {
                    blackListString += blackListArray.get(i).getId() + "#";
                }
                if (blackListString.length() != 0){
                    blackListString = blackListString.substring(0,blackListString.length() - 1);
                }

                output.print("partners:" + partnerString + ",");
                output.print("accommodation:"+accommodationString+",");
                output.print("blacklist:"+blackListString);

                output.println();
            }

            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void toUniversalFont(JPanel panel) {
    	Component[] componentList = panel.getComponents();
    	
    	for (Component component : componentList) {
    		component.setFont(DesignConstants.SMALL_FONT);
    	}
    	
        revalidate();
        repaint();
    }

    private class LoginPanel extends JPanel {
        BufferedImage image = null;
        private JTextField idField;
        private JTextField nameField;
        private JTextField passwordField;
        private JButton loginButton;
        private JButton createAccount;
        private JButton createToggle;
        private boolean createIsToggled;
        private JComboBox gradeOptions;
        private JLabel nameText;
        private JLabel gradeText;
        private JLabel errorLabel;

        LoginPanel() {
            this.setFocusable(false);
            this.setOpaque(true);
            this.setLayout(new GridBagLayout());

            try {
                image = ImageIO.read(new File("loginBackground.jpg"));
            } catch (Exception e) {
                e.printStackTrace();	
            }

            this.add(new InnerFrame());

            this.setVisible(true);

            createIsToggled = false;
        }

        public void paintComponent(Graphics g) {
            if (this.image != null) {
                g.drawImage(this.image, 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                g.setColor(new Color(0, 0, 0));
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
        }

        private class InnerFrame extends JPanel implements ActionListener {
            InnerFrame() {
        		this.setLayout(new GridBagLayout());
        		this.setVisible(true);
        		this.setBorder(new EmptyBorder(WINDOW_WIDTH / 40, WINDOW_WIDTH / 40,
        				WINDOW_WIDTH / 40, WINDOW_WIDTH / 40));
        		 this.setBackground(DesignConstants.MAIN_COLOUR);

        		errorLabel = new JLabel("");

        		createToggle = new JButton("Don't have an account? Click here to sign up.");
        		createToggle.addActionListener(this);

                addComponent(0, new JLabel("Student ID"));
               
        		idField = new JTextField();
        		addComponent(1, idField);
        		
        		addComponent(2, new JLabel("Password"));
        		
        		passwordField = new JTextField();
        		addComponent(3, passwordField);
        		
        		loginButton = new JButton("Login");
        		loginButton.addActionListener(this);
        		addComponent(8,loginButton);

        		addComponent(9 ,createToggle);
        		
        		String[] grades = {"9", "10", "11", "12"};
        		nameField = new JTextField(20);
        		gradeOptions = new JComboBox(grades);

        		createAccount = new JButton("Create Account");
        		createAccount.addActionListener(this);
            }
            
            public void addComponent(int gridy, JComponent component) {
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = gridy;

                if (component instanceof JLabel) {
                    c.insets = new Insets(10, 0, 0, 0); // space in between
                    c.anchor = GridBagConstraints.LINE_START;
                    component.setForeground(Color.WHITE);
                    component.setFont(DesignConstants.SMALL_FONT);
                } else if (component instanceof JTextField) {
                    c.insets = new Insets(5, 0, 0, 0);
                    c.fill = GridBagConstraints.HORIZONTAL;
                    component.setFont(DesignConstants.LARGE_FONT);
                } else if (component instanceof JComboBox) {
                    c.anchor = GridBagConstraints.LINE_START;
                } else if (component instanceof  JButton) {
                    c.insets = new Insets(5, 0, 0, 0);
                    c.anchor = GridBagConstraints.LINE_START;
                }

                this.add(component, c);
            }

            public void showError(String error){
                errorLabel = new JLabel(error);
                addComponent(10, errorLabel);
            }

            public void hideError(){
                this.remove(errorLabel);
            }

            public void actionPerformed(ActionEvent evt) {
                Object source = evt.getSource();
                String inputId = idField.getText();
                String inputPassword = passwordField.getText();
                Student enteredStudent = findStudentByID(inputId);

                hideError();

                if (source == loginButton) {
                    if (enteredStudent != null){
                        if (enteredStudent.getPassword().equals(inputPassword)) {
                            ticketPanel = new TicketPanel(enteredStudent); //show selected student
                            showTicket(); //switch to ticket panel
                        } else {
                            showError("Password Incorrect");
                        }
                    } else {
                        showError("Student doesn't exist");
                    }
                } else if (source == createAccount) {
                    String name = nameField.getText();
                    String grade = String.valueOf(gradeOptions.getSelectedIndex());
                    if (inputId.equals("")) {
                        showError("Id cannot be blank");
                    } else if (idExists(inputId)) {
                        showError("Id already taken");
                    } else if (inputPassword.equals("")) {
                        showError("Pasword cannot be blank");
                    } else if (name.equals("")){
                        showError("Name cannot be blank");
                    } else {
                        Student s = new Student(name, inputId, grade, inputPassword);
                        students.add(s);
                        writeStudents();
                        ticketPanel = new TicketPanel(s);
                        showTicket();
                    }
                } else if (source == createToggle) {
                    if (createIsToggled){
                        createToggle.setText("Don't have an account? Click here to sign up.");

                        this.remove(nameText);
                        this.remove(nameField);
                        this.remove(gradeText);
                        this.remove(gradeOptions);
                        this.remove(createAccount);

                        addComponent(8,loginButton);

                    } else {
                        createToggle.setText("Back to login");

                        this.remove(loginButton);

                        nameText = new JLabel("Name");
                        addComponent(4, nameText);
                        addComponent(5, nameField);

                        gradeText = new JLabel("Grade");
                        addComponent(6, gradeText);

                        addComponent(7, gradeOptions);

                        addComponent(8, createAccount);

                    }
                    createIsToggled = !createIsToggled;
                }

                revalidate();
                repaint();
            }

            public boolean idExists(String inputId) {
                for (Student s : students){
                    if (inputId.equals(s.getId())){
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private class TicketPanel extends JPanel{
        private Student selectedStudent;
        private ArrayList<Student> partners;
        private ArrayList<Student> blackList;
        BufferedImage image = null;

        private ButtonPanel upperPanel;
        
        private int X_PADDING = 200;
        
        TicketPanel(Student student) {

            try {
                image = ImageIO.read(new File("loginBackground.jpg"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (student != null) {
                this.setLayout(new BorderLayout());
                this.setVisible(true);
                this.selectedStudent = student;
                partners = student.getPartners();
                blackList = student.getBlacklist();
                
                //add all components
                upperPanel = new ButtonPanel();
                this.add(upperPanel, BorderLayout.NORTH);
                
                JPanel mainPanel = new JPanel(new GridBagLayout());
                mainPanel.setOpaque(false);
                
                CenterPanel centerPanel = new CenterPanel();
                //centerPanel.setBackground(Color.WHITE);
                
                JScrollPane scrollFrame = new JScrollPane(centerPanel);
                scrollFrame.setPreferredSize(new Dimension(600, 400));
                scrollFrame.setBorder(null);
                
                GridBagConstraints c = new GridBagConstraints();
                c.anchor = GridBagConstraints.CENTER;
                c.weighty = 1.0;
                c.fill = GridBagConstraints.VERTICAL;
                c.ipadx = X_PADDING;
                
                mainPanel.add(scrollFrame, c);
              //JScrollPane pane = new JScrollPane(centerPanel);

                this.add(mainPanel);
            }
        }
        
        /*
        public void paintComponent(Graphics g) {
        	super.paintComponent(g);
        	
        	this.revalidate();
        	this.repaint();
        }
        */
        
        private class ButtonPanel extends JPanel implements ActionListener {
            private JButton logout;
            private JButton showFloor;

            ButtonPanel() {
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setBorder(new EmptyBorder(10, 0, 5, 0));
                setFocusable(false);
                setBackground(DesignConstants.MAIN_COLOUR);

                add(Box.createRigidArea(new Dimension(1100, 0)));
                logout = new JButton("Logout "); // add name, call showLogin()
                logout.addActionListener(this);

                showFloor = new JButton("Floor Plan");
                showFloor.addActionListener(this);
                
                this.add(logout);
                this.add(showFloor);
                
                toUniversalFont(this);
                
                revalidate();
                repaint();
            }

            public void actionPerformed(ActionEvent evt) {
                Object source = evt.getSource();
                if (source == logout) {
                    showLogin();
                } else if (source == showFloor){
                    showFloor(ticketPanel);
                }
            }
        }
        
        class CenterPanel extends JPanel {
        	private int currentYPos = 1;
        	private ResolveNamesPanel resolvePanel;
        	private PartnerPanel partnerPanel;
        	private JLabel infoMessage = new JLabel();

        	CenterPanel() {
        		this.setVisible(true);
        		this.setOpaque(false);
        		this.setPreferredSize(new Dimension(500, 2000));
        		//this.setMaximumSize(new Dimension(WINDOW_WIDTH - X_PADDING * 2, WINDOW_HEIGHT));
                this.setLayout(new GridBagLayout());
                //this.setBackground(DesignConstants.BACK_COLOUR);
                
                partnerPanel = new PartnerPanel();
                resolvePanel = new ResolveNamesPanel();
				
                JLabel titleLabel = new JLabel("RHHS Prom Registration");
                initComponent(titleLabel, 10);
                titleLabel.setFont(DesignConstants.LARGE_FONT);

                GridBagConstraints c = new GridBagConstraints();
        		c.gridy = currentYPos;
                c.weightx = 1.0;
                c.fill = GridBagConstraints.HORIZONTAL;
                this.add(titleLabel, c);

                updateInfoLabel();
                initComponent(infoMessage, 5);

                c.gridy = currentYPos + 1;
                this.add(infoMessage, c);
                currentYPos += 2;

                addComponent(new JLabel("Student Preferences for Seating"));
                addComponent(partnerPanel);
                addComponent(new JLabel("Specify Students' IDs"));
                addComponent(resolvePanel);
                addComponent(new JLabel("Local School Cash"));
                addComponent(new PaymentPanel());
                addComponent(new AccommodationPanel());
                addComponent(new ProfilePanel(selectedStudent.getPicture()));
                //this.setSize(new Dimension(WINDOW_WIDTH * 3 / 7, WINDOW_HEIGHT));
        	}
        	
        	public void updateInfoLabel() {
        		if ((selectedStudent.hasPaid()) && (selectedStudent.getPartners().size() == 0)) {
                    infoMessage.setText("Woo! You're coming to Prom! Make sure to set your preferences");
                } else if (selectedStudent.hasPaid()) {
                    infoMessage.setText("Woo! You're set for Prom! You can still add more or edit your preferences");
                } else {
                    infoMessage.setText("You're almost there! Make sure to purchase your ticket");
                }
        	}

        	public void initComponent(JComponent label, int borderY) {
        		label.setBackground(Color.WHITE);
        		label.setOpaque(true);
        		label.setBorder(BorderFactory.createMatteBorder(borderY, 10, borderY, 10, Color.WHITE));
        	}

        	public void addComponent(JComponent component) {
        		int yInterPadding;
        		GridBagConstraints c = new GridBagConstraints();
        		if (component instanceof JLabel) {
        			yInterPadding = 20;
        			initComponent(component, 10);
                    component.setFont(DesignConstants.MEDIUM_FONT);
        		} else {
        			initComponent(component, 15);
        			yInterPadding = 5;
        		}
                c = new GridBagConstraints();
        		if (currentYPos == 10) {
        			c.anchor = GridBagConstraints.NORTHWEST;
        			c.weighty = 1.0;
        		} else {
        			c.anchor = GridBagConstraints.LINE_START;
        		}
        		
        		c.insets = new Insets(yInterPadding, 0, 0, 0);
        		c.gridy = currentYPos + 1;
                c.weightx = 1.0;
                c.fill = GridBagConstraints.HORIZONTAL;
                this.add(component, c);

                currentYPos++;
        	}
        	
        	private class PartnerPanel extends JPanel implements ActionListener {
            	private JTextField nameField;
                private JButton addPreferenceButton;
                private JLabel instructLabel = new JLabel("List of students you would like to sit with: ");
                private int editingIndex;
                
                private final String NEW_STUDENT_TEXT = "New Student's Name";
                
                PartnerPanel() {
                    this.setVisible(true);
                    //this.setLayout(new GridBagLayout());
                    //this.setOpaque(false);
                    this.setFocusable(true);
                    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
                    //this.setBackground(Color.WHITE);
                    
                    /*
            		nameField = new JTextField("New student's name");
            		nameField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
                    nameField.addMouseListener(new MouseAdapter() {
                    	@Override
                    	public void mouseClicked(MouseEvent e) {
                    		nameField.setText("");
                    	}
                    });
                    nameField.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyTyped(KeyEvent e) {
                            if (nameField.getText().length() >= 50) // limit to 50 characters
                                e.consume();
                        }
                    });
                    nameField.setMaximumSize(new Dimension(800, 50));
                    */
                    addPreferenceButton = new JButton("Add Partner");
                    addPreferenceButton.addActionListener(this);
                    
                    this.add(instructLabel);

                    if (partners.size() == 0) {
                    	RowPair partnerPair = new RowPair(NEW_STUDENT_TEXT, false);
                    	this.add(partnerPair);
                    } else {
    	                for (int i = 0; i < partners.size(); i++) {
    	                	RowPair partnerPair = new RowPair(partners.get(i).getName(), partners.get(i).getId(), false);
    	                	this.add(partnerPair);
    	                }
                    }

                    for (int i = 0; i < blackList.size(); i++) {
                        RowPair partnerPair = new RowPair(blackList.get(i).getName(), blackList.get(i).getId(), true);
                        this.add(partnerPair);
                    }
                    
                    editingIndex = -1;
                    
                    this.add(addPreferenceButton);
                    
                    this.setBorder(new EmptyBorder(10, 10, 10, 10));
                    toUniversalFont(this);
                }
                
                public int getIndex(PreferenceRow row) {
                	Component[] componentList = this.getComponents();

                	for (int i = 0; i < componentList.length; i++) {
                		if ((componentList[i] instanceof RowPair) && (((RowPair) componentList[i]).hasRow(row))) {
                			return i;
                		}
                	}
                	
                	return -1;
                }
                
                public boolean showFixed(int index) {
                	Component[] componentList = this.getComponents();
                	if (componentList[index] instanceof RowPair) {
                		if ((index == componentList.length - 2) && 
                				(((RowPair) componentList[index]).getFixedText().equals(NEW_STUDENT_TEXT))) {
                			this.remove(componentList[index]);
                		} else {
                			((RowPair) componentList[index]).toFixedVisibility(((RowPair) componentList[index]).getStudentID());
                		}
                		return true;
                	} else {
                		return false;
                	}
                }
            	
                private void addPairToBottom(String text, String id) {
                	Component[] componentList = this.getComponents();
                	
                	if (id == null) {
	                	if (editingIndex != -1) {
	        				showFixed(editingIndex);
	        			} 
	                	editingIndex = componentList.length - 1;
                	} else {
                		editingIndex = -1;
                	}
            		
        			this.remove(addPreferenceButton);
        			
        			RowPair pairToAdd;
        			if (id == null) {
        				pairToAdd = new RowPair(text, false);
        			} else {
        				pairToAdd = new RowPair(text, id, false);
        			}
                	this.add(pairToAdd);
                	
                	this.add(addPreferenceButton);
                	
                	this.revalidate();
                	this.repaint();
                }
                
            	private void removePair(RowPair pairToRemove) {
            		this.remove(pairToRemove);

                    Student s = findStudentByID(pairToRemove.getStudentID());

                    if (pairToRemove.isBlackList()){
                        blackList.remove(s);
                    } else {
                        partners.remove(s);
                    }

                	writeStudents();
            		
                	this.revalidate();
                	this.repaint();
            	}
                
                public void actionPerformed(ActionEvent e) {
                	Object source = e.getSource();
                	if (source == addPreferenceButton) {
                		Component[] componentList = this.getComponents();
                		int index = componentList.length - 2;
                		if (index == 0) {
                			RowPair partnerPair = new RowPair(NEW_STUDENT_TEXT, false);
                        	this.add(partnerPair);
                		} else {
    	            		addPairToBottom(NEW_STUDENT_TEXT, null);
                		}
                	}
                }
                
                private class RowPair extends JPanel {
                	private FixedRow fixed;
                	private EditingRow editing;
                	private String savedStudentID;
                    private boolean isBlackList;
                	
                	RowPair(String text, boolean isBlackList) {
                	    this.isBlackList = isBlackList;
                	    recolour();

                		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
                		
                		fixed = new FixedRow(text);
                		editing = new EditingRow(text);
                		
                		toEditingVisibility();
                		
                		this.add(fixed);
                		this.add(editing);
                	}
                	
                	RowPair(String text, String id, boolean isBlackList) {
                	    this.isBlackList = isBlackList;
                	    recolour();

                		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
                		
                		fixed = new FixedRow(text);
                		editing = new EditingRow(text);
                		
                		toFixedVisibility(id);
                		
                		this.add(fixed);
                		this.add(editing);
                	}
                	
                	public String getFixedText() {
                		return fixed.getText();
                	}
                	
                	private String getStudentID() {
                		return savedStudentID;
                	}
                	
                	private void copyToEditing() {
                		editing.setText(fixed.getText());
                	}

                	public void toFixedVisibility(String id) {
                		savedStudentID = id;
                		fixed.setText(editing.getText());
                		
                		fixed.setVisible(true);
                		editing.setVisible(false);
                	}
                	
                	private void toEditingVisibility() {
                		fixed.setVisible(false);
                		editing.setVisible(true);
                	}
                	
                	public boolean hasRow(PreferenceRow row) {
                		if ((row.equals(editing)) || (row.equals(fixed))) {
                			return true;
                		} else {
                			return false;
                		}
                	}
                	
                	public boolean editingIsVisible() {
                		if (editing.isVisible()) {
                			return true;
                		} else {
                			return false;
                		}
                	}
                	
                	private void removeThis() {
                		removePair(this);
                	}

                    public void recolour(){
                        if (isBlackList) {
                            this.setBackground(new Color(63, 63, 63));
                        } else {
                            this.setBackground(new Color(170, 181, 49));
                        }
                    }

                    public void setBlackList(boolean isBlackList){
                        this.isBlackList = isBlackList;
                    }

                    public boolean isBlackList(){
                        return this.isBlackList;
                    }

                    /**
                     * Finds all students with the inputted name.
                     * @param name The students name
                     * @return Students with that name or similar.
                     */
                    private ArrayList<Student> findStudentsWithName(String name) {
                        ArrayList<Student> studentsWithNameExact = new ArrayList<Student>();
                        ArrayList<Student> studentWithNameClose = new ArrayList<>();

                        for (Student student : students) {
                            if (student.getName().equals(name)) {
                                studentsWithNameExact.add(student);
                            } else if (student.getName().contains(name)) {
                                studentWithNameClose.add(student);
                            }
                        }

                        if (studentsWithNameExact.size() == 0) {
                            if (studentWithNameClose.size() == 0) {
                                return null;
                            } else {
                                return studentWithNameClose;
                            }
                        } else {
                            return studentsWithNameExact;
                        }
                    }

                    private class FixedRow extends PreferenceRow implements ActionListener {
                    	private Component rigid;
                    	private JButton editButton;
                    	private JLabel nameLabel;
                    	private JButton whiteBlackListToggle;

                    	FixedRow(String nameLabel) {
                    		super(nameLabel);
                    		this.setOpaque(false);

                    		this.nameLabel = new JLabel(nameLabel);
                    		
                    		editButton = new JButton("%"); // change later
                    		editButton.addActionListener(this);

                    		if (isBlackList()){
                                whiteBlackListToggle = new JButton("Make Preferred");
                            } else {
                                whiteBlackListToggle = new JButton("Make Blacklisted");
                            }
                            whiteBlackListToggle.addActionListener(this);

                    		this.add(this.nameLabel);

                    		rigid = Box.createRigidArea(new Dimension
                    				(SPACE_TO_BUTTONS - DesignConstants.metricsSmall.stringWidth(this.nameLabel.getText()), 0));
                    		this.add(rigid);
                    		this.add(editButton);
                    		this.add(removeButton);
                    		this.add(whiteBlackListToggle);
                    		this.add(Box.createHorizontalGlue());

                    		toUniversalFont(this);
                    	}
                    	
                    	public String getText() {	
                			return nameLabel.getText();
                    	}
                    	
                    	public void actionPerformed(ActionEvent e) {
                    		Object source = e.getSource();
                    		if (source == editButton) {
                    			copyToEditing();
                    			toEditingVisibility();
                    			
                    			if (editingIndex != -1) {
                    				showFixed(editingIndex);
                    			} 
                    			editingIndex = getIndex(this);
                    			
                    		} else if (source == removeButton) {
                    			removeThis();
                    		} else if (source == whiteBlackListToggle) {
                    		    setBlackList(!isBlackList());
                    		    recolour();
                                Student current = findStudentByID(savedStudentID);
                    		    if (isBlackList()) {
                                    partners.remove(current);
                                    blackList.add(current);
                                    whiteBlackListToggle.setText("Make Preferred");
                                } else {
                                    blackList.remove(current);
                                    partners.add(current);
                                    whiteBlackListToggle.setText("Make Blacklisted");
                                }
                    		    writeStudents();
                            }
                    	}

        				@Override
        				public void setText(String newText) {
        					nameLabel.setText(newText);
        					rigid = Box.createRigidArea(new Dimension
                    				(SPACE_TO_BUTTONS - DesignConstants.metricsSmall.stringWidth(this.nameLabel.getText()), 0));
        				}
                    }

                    private class EditingRow extends PreferenceRow implements ActionListener {
                    	private JButton okButton;
                    	private JTextField nameField;
                    	private final int NUM_COLUMNS = 20;

                    	EditingRow(String nameLabel) {
                    		super(nameLabel);
                    		//this.setBackground(Color.RED);
                    		
                    		nameField = new JTextField(nameLabel);
                    		nameField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
                            nameField.addMouseListener(new MouseAdapter() {
                            	@Override
                            	public void mouseClicked(MouseEvent e) {
                            		nameField.setText("");
                            	}
                            });
                            
                            nameField.setMaximumSize(new Dimension(SPACE_TO_BUTTONS, NUM_COLUMNS));
                            nameField.setColumns(20);
                    		/*
                            nameField.addKeyListener(new KeyAdapter() {
                                @Override
                                public void keyTyped(KeyEvent e) {
                                    if (nameField.getText().length() >= 50) // limit to 50 characters
                                        e.consume();
                                }
                            });
                    		*/
                    		
                    		okButton = new JButton("OK");
                    		okButton.addActionListener(this);
                    		
                    		this.add(nameField);
                    		this.add(Box.createRigidArea(new Dimension(
                    				SPACE_TO_BUTTONS - DesignConstants.metricsSmall.charWidth('m') * NUM_COLUMNS, 0)));
                    		this.add(okButton);
                    		this.add(removeButton);
                    		this.add(errorLabel);
                    		this.add(Box.createHorizontalGlue());
                    		toUniversalFont(this);
                    	}
                    	
                    	public String getText() {	
                			return nameField.getText();
                    	}
                    	
                    	public void actionPerformed(ActionEvent e) {                		
                    		Object source = e.getSource();
                    		if (source == okButton) {
                    			ArrayList<Student> foundStudents = findStudentsWithName(nameField.getText());
                    			if (foundStudents == null) {
                                    errorLabel.setText("<html> Partner not Found. "
                                    		+ "<br> Ask them to register before you can add them. </html>");
                                } else if (foundStudents.size() == 1) {
                                	if (foundStudents.get(0) == TicketPanel.this.selectedStudent) {
        	                            errorLabel.setText("You can't add yourself");
        	                        } else if ((partners.contains(foundStudents.get(0))) || (blackList.contains(foundStudents.get(0)))) {
        	                            toFixedVisibility(foundStudents.get(0).getId());
        	                            errorLabel.setText("Person already added");
        	                            
        	                            editingIndex = -1;
        	                        } else {
                                	    if (isBlackList) {
                                            blackList.remove(new Student(getFixedText(), getStudentID()));
                                        } else {
                                            partners.remove(new Student(getFixedText(), getStudentID()));
                                        }
        	                        	
        	                            toFixedVisibility(foundStudents.get(0).getId());
        	                            
        	                            editingIndex = -1;
        	                        	partners.add(foundStudents.get(0));
        	                        	setBlackList(false);
        	                        }
                                } else {
                                	errorLabel.setText("Warning! More than one student with that name");
                                	resolvePanel.addResolvingRow(foundStudents);
                                	removeThis();
                                }
                    		} else if (source == removeButton) {
                    			removeThis();
                    			editingIndex = -1;
                    		}

                    		recolour();
                    		writeStudents();

                        	this.revalidate();
                        	this.repaint();
                    	}

        				@Override
        				public void setText(String newText) {
        					nameField.setText(newText);
        				}
                    }
                }
                
                abstract public class PreferenceRow extends JPanel implements ActionListener {
                	protected JButton removeButton;
                	abstract public String getText();
                	abstract public void setText(String newText);
                	protected JLabel errorLabel;
                	protected final int SPACE_TO_BUTTONS = 300;
                	
                	PreferenceRow(String nameLabel) {
                		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
                		this.setFocusable(true);
                		
                		//this.setMaximumSize(new Dimension(WINDOW_WIDTH, 100));
                		this.setBorder(new EmptyBorder(3, 3, 3, 3));
                		this.setAlignmentX(Component.LEFT_ALIGNMENT);
                		
                		errorLabel = new JLabel();
                		errorLabel.setBorder(new EmptyBorder(0, 10, 0, 10));

                		removeButton = new JButton("-");
                		removeButton.addActionListener(this);
                	}

                	@Override
                	public boolean equals(Object obj) {
                		if (!obj.getClass().equals(this.getClass())) {
                			return false;
                		} else if (((PreferenceRow) obj).getText().equals(this.getText())) {
                			return true;
                		} else {
                			return false;
                		}
                	}
                }
        	}
        	
            public class ResolveNamesPanel extends JPanel {
            	private JLabel instructLabel;
            	private String EMPTY_PANEL_TEXT = "Your partner list does not currently require any additional information to be provided";
            	private String IN_USE_TEXT = "Please select which student you refer to";

            	ResolveNamesPanel() {
            		instructLabel = new JLabel(EMPTY_PANEL_TEXT);
            		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            		
            		this.add(instructLabel);
            	}

            	private class ResolveRow extends JPanel implements ActionListener{
                    ArrayList<String> studentIds = new ArrayList<String>();
                    ArrayList<JButton> studentSelectors = new ArrayList<JButton>();

            	    ResolveRow (ArrayList<Student> foundStudents) {
                        JScrollPane resolvingRowPanel = new JScrollPane();
                        resolvingRowPanel.setLayout(new FlowLayout());

                        for (Student student : foundStudents) {
                            JPanel selectorPane = new JPanel();
                            selectorPane.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
                            BufferedImage studentPicture = student.getPicture();
                            if (studentPicture != null){
                                selectorPane.add(new JLabel(new ImageIcon(studentPicture)));
                            } else {
                                selectorPane.add(new JLabel("Picture Not Found"));
                            }
                            selectorPane.add(new JLabel(student.getId()));
                            selectorPane.add(new JLabel(student.getName()));
                            JButton select = new JButton("Select");
                            select.addActionListener(this);
                            selectorPane.add(select);
                            studentSelectors.add(select);
                            studentIds.add(student.getId());
                            resolvingRowPanel.add(selectorPane);
                        }
                        this.add(resolvingRowPanel);
                    }

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Object source = e.getSource();
                        int index = studentSelectors.indexOf(source);
                        String id = studentIds.get(index);
                        Student partner = findStudentByID(id);

                    }
                }

            	public void addResolvingRow(ArrayList<Student> foundStudents) {
            		if (instructLabel.getText().equals(EMPTY_PANEL_TEXT)) {
            			instructLabel.setText(IN_USE_TEXT);
            		}
                    ResolveRow resolveRow = new ResolveRow(foundStudents);

            		this.add(resolveRow);
            	}
            }

            private class PaymentPanel extends JPanel implements ActionListener {
            	private JLabel cardLabel = new JLabel("Card Number");
            	private JTextField cardField = new JTextField();

                private JLabel codeLabel = new JLabel("Security Code");
                private JTextField codeField = new JTextField();

                private JLabel expiryLabel = new JLabel("Expiry Date");
                private JComboBox monthBox;
                private JComboBox yearBox;

                private JButton buyButton = new JButton("Buy now!");
                private JButton refundButton = new JButton("Click here for a refund");
                private int currentYPos = 0;

            	PaymentPanel() {
            		this.setLayout(new GridBagLayout());
                    //do all layout

            		this.setBorder(new EmptyBorder(10, 10, 10, 10));

                    // this.add(infoMessage, BorderLayout.CENTER);

            		buyButton.addActionListener(this);
            		refundButton.addActionListener(this);

                    cardField.setColumns(20);
            		addRow(cardLabel, cardField);

            		codeField.setColumns(10);
            		addRow(codeLabel, codeField);

            		this.add(expiryLabel, getLabelConstraints(expiryLabel));

            		String[] monthOptions = new String[13];
            		monthOptions[0] = "mm ";
            		for (int i = 1; i < 13; i++) {
            			monthOptions[i] = Integer.toString(i);
            		}
            		monthBox = new JComboBox(monthOptions);
            		GridBagConstraints c = new GridBagConstraints();
            		c.gridx = 1;
            		c.gridy = currentYPos;
            		this.add(monthBox, c);

            		c.gridx = 2;
            		c.insets = new Insets(0, 5, 0, 5);
            		this.add(new JLabel("/"), c);

            		String[] yearOptions = new String[15];
            		yearOptions[0] = "yy";
            		int curYear = Calendar.getInstance().get(Calendar.YEAR);
            		for (int i = 0; i < 12; i++) {
            			yearOptions[i + 1] = Integer.toString(curYear + i);
            		}
            		yearBox = new JComboBox(yearOptions);

            		c = new GridBagConstraints();
            		c.gridx = 3;
            		c.gridy = currentYPos;
            		c.anchor = GridBagConstraints.LINE_START;
            		c.weightx = 1.0;
            		this.add(yearBox, c);

            		c = new GridBagConstraints();
            		c.gridy = currentYPos + 1;
                    this.add(buyButton, c);

                    c.gridy = currentYPos + 2;
                    this.add(refundButton, c);

            		if (selectedStudent.hasPaid()){
                        setVisibility(false);
                    } else {
                        setVisibility(true);
                    }
            	}

            	public void addRow(JLabel label, JTextField field) {
                    this.add(label, getLabelConstraints(label));

                    GridBagConstraints c = new GridBagConstraints();
                    c.anchor = GridBagConstraints.LINE_START;
            		c.gridwidth = 3;
                    c.gridx = 1;
                    this.add(field, c);

                    currentYPos++;
            	}

            	public GridBagConstraints getLabelConstraints(JLabel label) {
            		GridBagConstraints c = new GridBagConstraints();
            		c.anchor = GridBagConstraints.LINE_START;
            		c.gridy = currentYPos;
            		c.insets = new Insets(0, 0, 20, 80 - label.getWidth());

            		return c;
            	}

            	public void setVisibility(boolean toBuy) {
            		Component[] componentList = this.getComponents();

            		for (Component component : componentList) {
            			if (!component.equals(refundButton)) {
            				component.setVisible(toBuy);
            			} else {
            				component.setVisible(!toBuy);
            			}
            		}
            	}

                @Override
                public void actionPerformed(ActionEvent e) {
                    Object source = e.getSource();
                    if (source == buyButton){
                        setVisibility(false);
                        selectedStudent.setPaid(true);
                    } else if (source == refundButton) {
                        setVisibility(true);
                        selectedStudent.setPaid(false);
                    }
                    writeStudents();
                    updateInfoLabel();
                }
            }

            private class ProfilePanel extends JPanel implements ActionListener{
                private BufferedImage studentImage;
                private JLabel imageComponent;
                private JFileChooser fileChooser;
                private JButton selectImage;
                private Path filePath;

                private static final int imageSize = 200;

                ProfilePanel(BufferedImage image){
                    this.setLayout(new GridBagLayout());

                    studentImage = image;

                    if (studentImage != null) {
                        imageComponent = new JLabel(new ImageIcon(studentImage));
                    } else {
                        imageComponent = new JLabel("No image");
                    }
                    selectImage = new JButton("Select Image");
                    fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));

                    GridBagConstraints c = new GridBagConstraints();
                    c.gridy = 0;
                    this.add(selectImage,c);
                    selectImage.addActionListener(this);

                    c = new GridBagConstraints();
                    c.gridy = 1;
                    this.add(imageComponent,c);

                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    Object source = e.getSource();
                    if (source == selectImage){
                        int returnValue = fileChooser.showOpenDialog(null);

                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            try {
                                File selectedFile = fileChooser.getSelectedFile();
                                filePath = selectedFile.toPath();

                                BufferedImage tempBufferedImage = ImageIO.read(new File(filePath.toString()));

                                int w = tempBufferedImage.getWidth();
                                int h = tempBufferedImage.getHeight();
                                int s;
                                if (w > h){
                                    s = h;
                                } else {
                                    s = w;
                                }

                                tempBufferedImage = tempBufferedImage.getSubimage((w-s)/2,(h-s)/2, s, s);
                                Image tempImage = tempBufferedImage.getScaledInstance(imageSize,imageSize, Image.SCALE_SMOOTH);

                                studentImage = new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_INT_ARGB);
                                Graphics2D resize = (Graphics2D) studentImage.getGraphics();
                                resize.drawImage(tempImage, 0, 0, null);
                                resize.dispose();

                                //remove old image
                                this.remove(imageComponent);
                                //add new image
                                imageComponent = new JLabel(new ImageIcon(studentImage));

                                GridBagConstraints c = new GridBagConstraints();
                                c.gridy = 1;

                                this.add(imageComponent,c);

                                selectedStudent.setPicture(studentImage);

                                writeStudents();
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }

                            revalidate();
                            repaint();
                        }
                    }
                }
            }

            private class AccommodationPanel extends JPanel implements ActionListener{
                private ArrayList<String> accommodations;
                private ArrayList<JButton> removes;
                private ArrayList<JLabel> labels;
                private JButton addAccommodation;
                private JTextField accommodationInput;

                AccommodationPanel() {
                    this.setVisible(true);
                    this.accommodations = selectedStudent.getAccommodations();
                    this.setLayout(new GridBagLayout());
                    removes = new ArrayList<JButton>();
                    labels = new ArrayList<JLabel>();
                    GridBagConstraints c;
                    for (int i = 0; i < this.accommodations.size(); i++) {
                        c = new GridBagConstraints();
                        c.gridx = 0;
                        c.gridy = i;
                        JButton r = new JButton("Remove");
                        r.addActionListener(this);
                        this.add(r,c);
                        removes.add(r);

                        c = new GridBagConstraints();
                        c.gridx = 1;
                        c.gridy = i;
                        JLabel l = new JLabel(this.accommodations.get(i));
                        this.add(l,c);
                        labels.add(l);
                   }

                   addAccommodation = new JButton("Add Accommodation");
                   accommodationInput = new JTextField("Accommodation Here");

                   c = new GridBagConstraints();
                   c.gridx = 0;
                   c.gridy = this.accommodations.size();
                   this.add(addAccommodation, c);
                   addAccommodation.addActionListener(this);

                   c = new GridBagConstraints();
                   c.gridx = 1;
                   c.gridy = this.accommodations.size();
                   this.add(accommodationInput, c);
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    Object source = e.getSource();

                    this.remove(addAccommodation);
                    this.remove(accommodationInput);

                    if ((source == addAccommodation) && (!accommodationInput.getText().equals("Accommodation Here"))){

                        GridBagConstraints c = new GridBagConstraints();
                        c.gridx = 0;
                        c.gridy = accommodations.size();
                        JButton r = new JButton("Remove");
                        r.addActionListener(this);
                        this.add(r,c);
                        removes.add(r);

                        c = new GridBagConstraints();
                        c.gridx = 1;
                        c.gridy = this.accommodations.size();
                        JLabel l = new JLabel(accommodationInput.getText());
                        this.add(l,c);
                        labels.add(l);

                        accommodations.add(accommodationInput.getText());

                        accommodationInput.setText("Accommodation Here");

                        writeStudents();
                    } else {
                        int index = removes.indexOf(source);

                        if (index != -1) {
                            this.removeAll();

                            removes.remove(index);
                            labels.remove(index);
                            accommodations.remove(index);

                            GridBagConstraints c;
                            for (int i = 0; i < this.accommodations.size(); i++) {
                                c = new GridBagConstraints();
                                c.gridx = 0;
                                c.gridy = i;

                                this.add(removes.get(i),c);

                                c = new GridBagConstraints();
                                c.gridx = 1;
                                c.gridy = i;

                                this.add(labels.get(i),c);
                            }

                            writeStudents();
                        }
                    }

                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx = 0;
                    c.gridy = this.accommodations.size();
                    this.add(addAccommodation, c);
                    addAccommodation.addActionListener(this);

                    c = new GridBagConstraints();
                    c.gridx = 1;
                    c.gridy = this.accommodations.size();
                    this.add(accommodationInput, c);

                    revalidate();
                    repaint();
               }
           }
        }

        public void paintComponent(Graphics g) {
            if (this.image != null) {
                g.drawImage(this.image, 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                g.setColor(new Color(0, 0, 0));
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
        }
    }

    /**
     * The floor plan panel. Displays the floor plan.
     */
    private class FloorPlanPanel extends JPanel implements ActionListener{
        private JPanel fromPanel;
        private JButton exitButton;

        /**
         * The constructor for the FloorPlanPanel. Adds an exit button and a FloorPlanSystem.
         * @param fromPanel the panel that called the floor plan.
         * @see FloorPlanSystem
         */
        FloorPlanPanel(JPanel fromPanel) {
            HashMap<Student, HashMap<Student, Double>> paidHash = new HashMap<Student, HashMap<Student, Double>>();
            ArrayList<Student> paidStudents = new ArrayList<Student>();

            for (Student s : students){
                if (s.hasPaid() && paidHash.size() < Prom.maxTables*Prom.tableSize){
                    HashMap<Student, Double> weights = new HashMap<Student, Double>();
                    for (Student partner : s.getPartners()){
                        weights.put(partner, 1.0);
                    }
                    for (Student blackList : s.getBlacklist()){
                        weights.put(blackList, -100.0);
                    }
                    paidHash.put(s, weights);
                    paidStudents.add(s);
                }
            }
            tables = SeatingAssignmentSystem.assignTables(paidStudents, Prom.maxTables, Prom.tableSize, paidHash);
            for (Table t: tables){
                System.out.print("[");
                for (Student s : t.getStudents()){
                    System.out.print(s.getId() + ",");
                }
                System.out.println("]");
            }
            floorPlan = new FloorPlanSystem(tables);
            this.fromPanel = fromPanel;
            this.exitButton = new JButton("Hide FloorPlan");
            this.setLayout(new BorderLayout());
            this.add(floorPlan, BorderLayout.CENTER);
            this.add(exitButton, BorderLayout.PAGE_START);
            this.exitButton.addActionListener(this);
        }

        /**
         * Monitors for the exit button being pressed.
         * @param e the button pressed.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == exitButton){
                if (this.fromPanel == loginPanel){
                    showLogin();
                } else if (this.fromPanel == ticketPanel){
                    showTicket();
                }
            }
        }
    }
    
    public static final class DesignConstants {
		public static final Color BACK_COLOUR = Color.BLACK;//new Color((float) (130 / 255.0), (float) (235 / 255.0), 
    			//(float) (33 / 255.0), (float) 0.3);
        public static final Color MAIN_COLOUR = new Color(75, 112, 68);
        
        public static final Font LARGE_FONT = new Font("Segoe UI", Font.PLAIN, 20);
        public static final Font MEDIUM_FONT = new Font("Segoe UI", Font.BOLD, 15);
        public static final Font SMALL_FONT = new Font("Segoe UI", Font.BOLD, 13);
        public static final Canvas C = new Canvas();
        public static final FontMetrics metricsSmall = C.getFontMetrics(SMALL_FONT);

        DesignConstants() {
        	throw new AssertionError();
        }
    }
}