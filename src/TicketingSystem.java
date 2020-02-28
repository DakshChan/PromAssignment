import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.nio.file.Path;
import java.util.*;

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

    private void showTicket() {
        this.removeAll();
        this.add(this.ticketPanel);
        this.ticketPanel.setVisible(true);
        this.ticketPanel.requestFocus();
        this.updateUI();
    }

    private void showLogin() {
        this.removeAll();
        this.loginPanel = new LoginPanel(); //reset the login panel
        this.add(this.loginPanel);
        this.loginPanel.setVisible(true);
        this.loginPanel.requestFocus();
        this.updateUI();
    }

    private void showFloor(JPanel fromPanel){
        floorPanel = new FloorPlanPanel(fromPanel);
        this.removeAll();
        this.add(this.floorPanel);
        this.floorPanel.setVisible(true);
        this.floorPanel.requestFocus();
        this.updateUI();
    }

    private void addStudent(Student student) {
        this.students.add(student);
    }

    private void removeStudent(Student student) {
        this.students.remove(student);
    }

    private Student findStudentByName(String name){
        for (Student student : students){
            if (name.compareToIgnoreCase(student.getName()) == 0){
                return student;
            }
        }
        return null;
    }

    private Student findStudentByID(String id) {
        for (Student student : students){
            if (student.getId().equals(id)){
                return student;
            }
        }
        return null;
    }

    private void initializeStudents() {
        try {
            Scanner input = new Scanner(loginCredentials);
            ArrayList<String[]> partners = new ArrayList<String[]>();
            ArrayList<String[]> blackList = new ArrayList<String[]>();

            while (input.hasNext()) {
                String student = input.nextLine();
                if (!student.equals("")) {
                    String[] ids = student.split(",");
                    HashMap<String, String> keys = new HashMap();
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
        private Font fieldFont;
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
        		 this.setBackground(new Color(75, 112, 68));

        		fieldFont = new Font("Open Sans", Font.PLAIN, 20);

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
                } else if (component instanceof JTextField) {
                    c.insets = new Insets(5, 0, 0, 0);
                    c.fill = GridBagConstraints.HORIZONTAL;
                    component.setFont(fieldFont);
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

        private JLabel infoMessage = new JLabel();
        private ButtonPanel upperPanel;
        
        private int X_PADDING = 200;
        
        TicketPanel(Student student) {
            if (student != null) {
                this.setLayout(new BorderLayout());
                this.setVisible(true);
                this.selectedStudent = student;
                partners = student.getPartners();
                
                //add all components
                upperPanel = new ButtonPanel();
                this.add(upperPanel, BorderLayout.NORTH);
                
                JPanel mainPanel = new JPanel(new GridBagLayout());
                mainPanel.setBackground(new Color(235, 255, 246));
                
                CenterPanel centerPanel = new CenterPanel();
                centerPanel.setBackground(Color.WHITE);
                
                JScrollPane scrollFrame = new JScrollPane(centerPanel);
                centerPanel.setAutoscrolls(true);
                scrollFrame.setMaximumSize(new Dimension(400, 400));
                
                GridBagConstraints c = new GridBagConstraints();
                c.anchor = GridBagConstraints.CENTER;
                c.weighty = 1.0;
                c.fill = GridBagConstraints.VERTICAL;
                c.ipadx = X_PADDING;
                
                mainPanel.add(scrollFrame, c);
              //JScrollPane pane = new JScrollPane(centerPanel);

                this.add(mainPanel);
                /*
                if ((selectedStudent.hasPaid()) && (selectedStudent.getPartners().size() == 0)) {
                    infoMessage.setText("Woo! You're coming to Prom!\nMake sure to set your preferences");
                } else if (selectedStudent.hasPaid()) {
                    infoMessage.setText("Woo! You're set for Prom!\nYou can still add more or edit your preferences");
                } else {
                    infoMessage.setText("You're almost there!\nMake sure to purchase your ticket");
                }
                cardNumber.setText("This is the card number input");
				*/

            }
        }
        
        class CenterPanel extends JPanel {
        	private JLabel title = new JLabel("Student Preferences for Seating");
        	private PartnerPanel partnerPanel;
        	
        	CenterPanel() {
        		this.setVisible(true);
        		//this.setOpaque(false);
        		//this.setMaximumSize(new Dimension(WINDOW_WIDTH - X_PADDING * 2, WINDOW_HEIGHT));
                this.setLayout(new GridBagLayout());
                this.setBackground(new Color(235, 255, 246));
                
                title.setOpaque(true);
                title.setBorder(new EmptyBorder(10, 10, 10, 10));
                
                partnerPanel = new PartnerPanel();
				
                GridBagConstraints c = new GridBagConstraints();
                c.anchor = GridBagConstraints.NORTHWEST;
                c.weighty = 0.01;
                c.weightx = 1.0;
                c.fill = GridBagConstraints.HORIZONTAL;
                this.add(title, c);
                
                c.gridy = 1;
                c.weighty = 0.05; // changes next component
                c.weightx = 1.0; 
                this.add(partnerPanel, c);

                c.gridy = 2;
                c.weighty = 0.01;

                title = new JLabel("Local School Cash");
                title.setOpaque(true);
                title.setBorder(new EmptyBorder(10, 10, 10, 10));
                this.add(title, c);


                PaymentPanel paymentPanel = new PaymentPanel();
                c.gridy = 3;
                c.weighty = 1.0;
                this.add(paymentPanel, c);

                //Daksh added this stuff and he has no idea how to lay it out properly
                c = new GridBagConstraints();
                c.gridy = 4;
                c.weightx = 1.0;
                c.weighty = 1.0;
                c.anchor = GridBagConstraints.NORTHWEST;
                this.add(new AccommodationPanel(),c);


                c.gridy = 5;
                this.add(new ProfilePanel(selectedStudent.getPicture()),c);

                //this.setSize(new Dimension(WINDOW_WIDTH * 3 / 7, WINDOW_HEIGHT));
        	}
        }
        
        private class PartnerPanel extends JPanel implements ActionListener {
        	private JTextField nameField;
            private JButton addPreferenceButton;
            private JLabel instructLabel = new JLabel("List of students you would like to sit with: ");
            
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
                
                EditingRow row;
                if (partners.size() == 0) {
	            	this.add(new EditingRow("New student's name"));
                } else {
	                for (int i = 0; i < partners.size(); i++) {
	                	this.add(new FixedRow(partners.get(i).getName()));

	                	row = new EditingRow(partners.get(i).getName());
	                	this.add(row);
	                	row.setVisible(false);
	                }
                }
                
                this.add(addPreferenceButton);
                addPreferenceButton.addActionListener(this);
                
                this.setBorder(new EmptyBorder(10, 10, 10, 10));
            }
            
            /*
            public void showEditingRow(FixedRow row) {
            	Component[] componentList = this.getComponents();
            	int index = getIndex(componentList, row);

    			componentList[index + 1].setVisible(true);
    			((PreferenceRow) componentList[index + 1]).setText(row.getText());
            }
            
            public void showFixedRow(EditingRow row) {
            	Component[] componentList = this.getComponents();
            	int index = getIndex(componentList, row);

            	FixedRow rowToShow = (FixedRow) componentList[index - 1];
            	rowToShow.setText(row.getText());
            	rowToShow.setVisible(true);
            }
            */
            
            public PreferenceRow toggleRowState(PreferenceRow row) {
            	Component[] componentList = this.getComponents();
            	int index = getIndex(componentList, row);
            	
            	PreferenceRow rowToShow;
            	if (row instanceof EditingRow) {
            		rowToShow = (FixedRow) componentList[index - 1];
            	} else {
            		rowToShow = (EditingRow) componentList[index + 1];
            	}
            	
            	rowToShow.setText(row.getText());
            	rowToShow.setVisible(true);
            	
            	return rowToShow;
            }

            public int getIndex(Component[] componentList, PreferenceRow row) {
            	int rowIndex = 0;
            	while (!row.equals(componentList[rowIndex])) {
            		rowIndex++;
            	}
            	
            	return rowIndex;
            }
            
            public void removeRow(PreferenceRow row) {
            	row.setVisible(false);
            	
            	for (int j = 0; j < partners.size(); j++) {
    				if (partners.get(j).getName().equals(row.getText())) {
    					partners.remove(j);
    				}
    			}
            	writeStudents();
            	
        		Component[] componentList = this.getComponents();
        		// changes last one to EditingRow
        		if (componentList[componentList.length - 2].isVisible()) {
        			this.remove(addPreferenceButton);
        			componentList[componentList.length - 2].setVisible(false);
        			componentList[componentList.length - 3].setVisible(true);

                	this.add(addPreferenceButton);
                	addPreferenceButton.addActionListener(this);
        		}
            	
            	this.revalidate();
            	this.repaint();
            }
            
            public void actionPerformed(ActionEvent e) {
            	Object source = e.getSource();
            	if (source == addPreferenceButton) {
            		Component[] componentList = this.getComponents();
            		// adds new editing row
            		if (!(componentList[componentList.length - 2].isVisible())) {
            			PreferenceRow currentRow = new FixedRow("");

            			this.remove(addPreferenceButton);

            			this.add(currentRow);
                    	currentRow.setVisible(false);

            			currentRow = new EditingRow("New student's name");
                    	this.add(currentRow);
                    	
                    	this.add(addPreferenceButton);
                    	addPreferenceButton.addActionListener(this);
            		}
            	}
            	
            	writeStudents();
            	
            	this.revalidate();
            	this.repaint();
            }
            
            private class FixedRow extends PreferenceRow implements ActionListener {
            	private JButton editButton;
            	private JLabel nameLabel;
            	private JLabel personAddedLabel;

            	FixedRow(String nameLabel) {
            		super(nameLabel);
            		
            		this.setBackground(Color.BLACK);
            		this.nameLabel = new JLabel(nameLabel);
            		
            		editButton = new JButton("%"); // change later
            		editButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
            		editButton.addActionListener(this);

            		this.add(this.nameLabel);
            		this.add(Box.createHorizontalGlue());
            		this.add(editButton);
            		this.add(removeButton);
            	}
            	
            	public String getText() {	
        			return nameLabel.getText();
            	}
            	

            	
            	public void actionPerformed(ActionEvent e) {
            		super.actionPerformed(e);
            		
            		Object source = e.getSource();
            		if (source == editButton) {
            			this.setVisible(false);
            			toggleRowState(this);
            		}
            	}

				@Override
				public void setText(String newText) {
					nameLabel.setText(newText);
				}
            }
            
            private class EditingRow extends PreferenceRow implements ActionListener {
            	private JButton okButton;
            	private JTextField nameField;
            	
            	EditingRow(String nameLabel) {
            		super(nameLabel);
            		this.setBackground(Color.WHITE);
            		
            		nameField = new JTextField(nameLabel);
            		nameField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
                    nameField.addMouseListener(new MouseAdapter() {
                    	@Override
                    	public void mouseClicked(MouseEvent e) {
                    		nameField.setText("");
                    	}
                    });
                    nameField.setMaximumSize(new Dimension(300, 100));
                    //nameField.setMaximumSize(new Dimension(1, 50));
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
            		
            		okButton = new JButton("OK");
            		okButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
            		okButton.addActionListener(this);
            		
            		this.add(nameField);
            		this.add(Box.createHorizontalGlue());
            		this.add(okButton);
            		this.add(removeButton);
            	}
            	
            	public String getText() {	
        			return nameField.getText();
            	}
            	
            	public void actionPerformed(ActionEvent e) {
            		super.actionPerformed(e);
            		
            		Object source = e.getSource();
            		if (source == okButton) {
            			Student foundStudent = findStudentByName(nameField.getText());
                		// remove error?
                        if (foundStudent == TicketPanel.this.selectedStudent) {
                            this.addErrorLabel("You can't add yourself");
                        } else if (TicketPanel.this.selectedStudent.getPartners().contains(foundStudent)) {
                            this.setVisible(false);
                            PreferenceRow row = toggleRowState(this);
                            row.addErrorLabel("Person already added");
                        } else if (foundStudent == null) {
                            this.addErrorLabel("Partner not Found. Ask them to register before you can add them");
                        } else {
                        	this.setVisible(false);
                        	toggleRowState(this);
                        	
                        	partners.add(foundStudent);
                        }
            		}
                	writeStudents();
                	
                	this.revalidate();
                	this.repaint();
            	}

				@Override
				public void setText(String newText) {
					nameField.setText(newText);
				}
            }
            
            abstract private class PreferenceRow extends JPanel implements ActionListener {
            	protected JButton removeButton;
            	abstract public String getText();
            	abstract public void setText(String newText);
            	private JLabel errorLabel;
            	private final int RIGHT_BORDER = 400;
            	
            	PreferenceRow(String nameLabel) {
            		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            		this.setFocusable(true);
            		
            		//this.setMaximumSize(new Dimension(WINDOW_WIDTH, 100));
            		this.setBorder(new EmptyBorder(3, 3, 3, RIGHT_BORDER));
            		this.setAlignmentX(Component.LEFT_ALIGNMENT);
            		
            		removeButton = new JButton("-");
            		removeButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
            		removeButton.addActionListener(this);
            	}
            	
            	public void actionPerformed(ActionEvent e) {
            		Object source = e.getSource();
            		if (source == removeButton) {
            			removeRow(this);
            		}
            	}
            	
            	public void addErrorLabel(String errorText) {
            		if (errorLabel == null) {
            			errorLabel = new JLabel(errorText);
            			//personAddedLabel = new JLabel("Person already added");
            			this.add(Box.createRigidArea(new Dimension(5, 0)));
            			this.add(errorLabel);
            			System.out.println(RIGHT_BORDER - errorLabel.getPreferredSize().getWidth());
            			this.setBorder(new EmptyBorder(3, 3, 3,
            					(int) (RIGHT_BORDER - errorLabel.getPreferredSize().getWidth())));
            		} else {
            			errorLabel.setText(errorText);;
            		}
            	}
            	
            	@Override
            	public boolean equals(Object obj) {
            		if (!obj.getClass().equals(this.getClass())) {
            			//System.out.println("not ")
            			return false;
            		} else if (((PreferenceRow) obj).getText().equals(this.getText())) {
            			return true;
            		} else {
            			return false;
            		}
            	}
            }
        }

        private class ButtonPanel extends JPanel implements ActionListener {
            private JButton logout;
            private JButton showFloor;

            ButtonPanel() {
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setBorder(new EmptyBorder(10, 0, 5, 0));
                setOpaque(false);
                setFocusable(false);

                add(Box.createRigidArea(new Dimension(1100, 0)));
                logout = new JButton("Logout "); // add name, call showLogin()
                logout.addActionListener(this);

                showFloor = new JButton("Floor Plan");
                showFloor.addActionListener(this);

                this.add(logout);
                this.add(showFloor);

                revalidate();
                repaint();
            }

            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;

                GradientPaint blackToGray = new GradientPaint(0, 0, new Color(25, 38, 23),
                        0, getHeight(), new Color(147, 222, 135));
                g2.setPaint(blackToGray);
                g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
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

        private class PaymentPanel extends JPanel implements ActionListener {
            //private JTextField cardNumber = new JTextField();
            private JLabel cardLabel = new JLabel("Enter Card Number:");
            private JButton buyButton = new JButton("Buy now!");
            private JButton refundButton = new JButton("Click here for a refund");

        	PaymentPanel() {
        		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
                //do all layout

        		this.setBorder(new EmptyBorder(10, 10, 10, 10));

                // this.add(infoMessage, BorderLayout.CENTER);

        		buyButton.addActionListener(this);
        		refundButton.addActionListener(this);

                this.add(cardLabel);
                //this.add(cardNumber);
                this.add(buyButton);
                this.add(refundButton);
        	}

            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == buyButton){
                    cardLabel.setVisible(false);
                    //cardNumber.setVisible(false);
                    buyButton.setVisible(false);
                    refundButton.setVisible(true);
                    selectedStudent.setPaid(true);

                } else if (source == refundButton) {
                    cardLabel.setVisible(true);
                    //cardNumber.setVisible(true);
                    buyButton.setVisible(true);
                    refundButton.setVisible(false);
                    selectedStudent.setPaid(false);
                }
                if (selectedStudent.hasPaid() && selectedStudent.getPartners().size() == 0){
                    infoMessage.setText("Woo! You're coming to Prom!\nMake sure to set your preferences");
                } else if (selectedStudent.hasPaid()) {
                    infoMessage.setText("Woo! You're set for Prom!\nYou can still add more or edit your preferences");
                } else {
                    infoMessage.setText("You're almost there!\nMake sure to purchase your ticket");
                }
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

    private class FloorPlanPanel extends JPanel implements ActionListener{
        private JPanel fromPanel;
        private JButton exitButton;

        FloorPlanPanel(JPanel fromPanel) {
            ArrayList<Student> paidStudents = new ArrayList<Student>();
            for (Student s : students){
                if (s.hasPaid() && paidStudents.size() < Prom.maxTables*Prom.tableSize){
                    paidStudents.add(s);
                }
            }
            tables = SeatingAssignmentSystem.assignTables(paidStudents, Prom.maxTables, Prom.tableSize);
            floorPlan = new FloorPlanSystem(tables);
            this.fromPanel = fromPanel;
            this.exitButton = new JButton("Hide FloorPlan");
            this.setLayout(new BorderLayout());
            this.add(floorPlan, BorderLayout.CENTER);
            this.add(exitButton, BorderLayout.PAGE_START);
            this.exitButton.addActionListener(this);
        }

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
}