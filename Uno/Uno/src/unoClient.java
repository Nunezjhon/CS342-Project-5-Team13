

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class unoClient extends Application {

	// signals (string constants)
    public static final String WEL_SIG = "welcome";           // client has connected to server
    public static final String WAIT_SIG = "wait";             // client is the first to connect to server
    public static final String TURN_SIG = "your move";        // client's turn
    public static final String PLAY_SIG = "client played";    // client made a play; server needs to read/process it
    
    public static final String LOSE_SIG = "lose";             // client lost round/game
    
    public static final String OP_SIG = "op:";                // opponent's score
    public static final String END_SIG = "over";              // game is over
    public static final String YES_SIG = "yes:";              // client wants re-match
    public static final String NO_SIG = "no:";                // client doesn't want re-match
    public static final String FORCE_SIG = "force:";          // force client disconnect
    public static final String UPDATE_SIG = "update list"; 	  // update list of clients 
    public static final String CLEAR_SIG = "clear list";	  // clears list of clients	
    public static final String CHALLENGE_SIG = "challenge:";  // challenge an opponent from our list
    public static final String REJECT_SIG = "reject"; 		  // reject challenge
    //public static final String DISABLE_SIG = "disable";		  // disable ability to challenge another client 	
    
    public static final String CARD_SIG = "card:";			  // signal for cards given by the server
    public static final String SCREEN_SIG = "update screen";  // update card pictures displayed
    public static final String PLAYED_SIG = "played:";    	  // card client played
    public static final String WINNER_SIG = "winner:";        // client won game
    public static final String PASS_SIG = "pass";			  // player passed
    public static final String TIE_SIG = "tie";               // both clients tied
    public static final String WIN_SIG = "win";               // client won round/game
    public static final String QUIT_SIG = "quit";             // close window signal
    
    
    
    // primitives/strings
    private final int WIN_WIDTH = 550;                  // width of the UI window
    private final int WIN_HEIGHT = 680;                 // height of the UI window
    private final int IMG_HEIGHT = 85;                  // width/height of images
    private final int IMG_WIDTH = 60;
    private final int NUM_PLAYS = 7;                    // number of hand signal choices
    private final String IMG_PATH = "/images/";         // relative path to image files
    private int port;                                   // port to listen on
    private int id;                                     // identifies client to server; 0 or 1
    private String address;                             // IP address of server to which the client is connecting
    private volatile String last;                       // last message from server
    private ObservableList<Integer> clientList;         // list of clients connected
    
    private volatile int cardsPlayed = 0;				// amount of cards played
    private volatile int timesPassed = 0;				// amount of times passed
    
    private ArrayList<cardPic> cardPics = new ArrayList<cardPic>(); // card images
    private ArrayList<unoCard> cards = new ArrayList<unoCard>();	// list of client card objects
    private unoDeck deck = new unoDeck();							// deck of cards (for identifying dealt cards)
    private cardImages cardPhotos = new cardImages();
    private volatile unoCard cardOnTop = new unoCard();					// the card on top of the deck after a play
    
    
    
    
    
    // UI
    private Stage myStage;
    private Scene menu;
    private VBox vbox;
    private HBox playBox;
    private HBox portBox;
    private HBox ipBox;
    private HBox reBox;
    private HBox opponentBox;
    private HBox mainImageBox;
    private Label message;
    private Label portText;
    private Label ipText;
    private Label rematch;
    private TextField portField;
    private TextField ipField;
    private Button[] plays;
    private Button portEnter;
    private Button ipEnter;
    private Button connect;
    private Button yes;
    private Button no;
    private Button pass;
    private Image[] images;
    private ImageView mainImage;
    private Image main;
    
    //private ComboBox opponents;
    private Text ID;
    
    // TCP/IP
    private Socket socket;      // connection end-point
    private Scanner in;         // reads input from server
    private PrintWriter out;    // writes output to server
  
    //@SuppressWarnings("unchecked")
	@Override
    public void init() { // initialize all UI objects

        // vbox
        vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(5);
    
        // game info     
        message = new Label("Enter address and port");
        message.setPrefSize(200, 50);
        message.setAlignment(Pos.CENTER);
        message.setFont(new Font(17));
        message.setTextFill(Color.rgb(210, 39, 30));
        
        ID = new Text();
        ID.setFont(new Font(15));
        ID.setText("ID: ");
        vbox.getChildren().add(ID);
        //vbox.getChildren().addAll(scoreText, otherScore, otherPlay, message);
        vbox.getChildren().addAll(message);
        
        
        
        // images
        images = new Image[NUM_PLAYS];
        
        
        images[0] = new Image(IMG_PATH + "card_back_large.png", IMG_WIDTH, IMG_HEIGHT, false, true);
        images[1] = new Image(IMG_PATH + "card_back_large.png", IMG_WIDTH, IMG_HEIGHT, false, true);
        images[2] = new Image(IMG_PATH + "card_back_large.png", IMG_WIDTH, IMG_HEIGHT, false, true);
        images[3] = new Image(IMG_PATH + "card_back_large.png", IMG_WIDTH, IMG_HEIGHT, false, true);
        images[4] = new Image(IMG_PATH + "card_back_large.png", IMG_WIDTH, IMG_HEIGHT, false, true);
        images[5] = new Image(IMG_PATH + "card_back_large.png", IMG_WIDTH, IMG_HEIGHT, false, true);
        images[6] = new Image(IMG_PATH + "card_back_large.png", IMG_WIDTH, IMG_HEIGHT, false, true);
        
        
        
        main = new Image(IMG_PATH + "card_back_large.png", 150, 200, false, true);
        mainImage = new ImageView(main);
        
        mainImageBox = new HBox(mainImage);
        mainImageBox.setPadding(new Insets(0,0,20,0));
        mainImageBox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(mainImageBox);
        
        // play buttons
        plays = new Button[NUM_PLAYS];
        playBox = new HBox();
        playBox.setSpacing(2);
        playBox.setPadding(new Insets(0, 0, 0, 0));
        playBox.setAlignment(Pos.CENTER);
        initButtons();
        vbox.getChildren().add(playBox);

        //Challenge button
        pass = new Button();
        pass.setText("Pass");
        pass.setDisable(true);
        pass.setOnAction(e -> {
        
        	pass.setDisable(true);
        	
        	
        	send(PASS_SIG);
        	message.setText("Opponent's turn");
        	if(timesPassed < 3) {
	        	delay();
	        	send(PLAY_SIG);
	        	send("" + id);
        	}
        	
        });
      
        // Opponent list
        clientList = FXCollections.observableArrayList();
        
        opponentBox = new HBox(pass);
        opponentBox.setSpacing(5);
        opponentBox.setPadding(new Insets(0,0,0,0));
        opponentBox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(opponentBox);
        
        // address entry
        ipText = new Label("IP");
        ipText.setPrefSize(30, 20);
        ipField = new TextField("127.0.0.1");
        ipField.setPrefSize(75, 20);
        ipEnter = new Button("Enter");
        ipEnter.setPrefSize(50, 20);
        ipEnter.setOnAction(e -> {

        	ipEnter.setDisable(true);
        	ipField.setDisable(true);
        	portEnter.setDisable(false);
        	portField.setDisable(false);
        	
            address = ipField.getText();

            if (port > 0) { // enable connect if an address and a port are given
                connect.setDisable(false);
            }

        });
        ipBox = new HBox();
        ipBox.setAlignment(Pos.CENTER);
        ipBox.setSpacing(5);
        ipBox.setPadding(new Insets(20, 0, 0, 0));
        ipBox.getChildren().addAll(ipText, ipField, ipEnter);
        vbox.getChildren().add(ipBox);

        // port entry
        portText = new Label("Port");
        portText.setPrefSize(30, 20);
        portField = new TextField("7777");
        portField.setPrefSize(75, 20);
        portEnter = new Button("Enter");
        portEnter.setPrefSize(50, 20);
        portEnter.setDisable(true);
    	portField.setDisable(true);
        
        
        portEnter.setOnAction(e -> {

        	portEnter.setDisable(true);
        	portField.setDisable(true);
            port = Integer.parseInt(portField.getText());

            if (address != null && !address.equals("")) { // enable connect if an address and a port are given
                connect.setDisable(false);
            }

        });
        portBox = new HBox();
        portBox.setAlignment(Pos.CENTER);
        portBox.setSpacing(5);
        portBox.setPadding(new Insets(0, 0, 20, 0));
        portBox.getChildren().addAll(portText, portField, portEnter);
        vbox.getChildren().add(portBox);

        // connect button
        connect = new Button("Connect");
        connect.setDisable(true);
        connect.setPrefSize(75, 20);
        setConnect();
        vbox.getChildren().add(connect);

        // rematch label
        rematch = new Label("Rematch?");
        rematch.setPrefSize(100, 20);
        rematch.setAlignment(Pos.CENTER);
        rematch.setPadding(new Insets(20, 0, 0, 0));
        rematch.setVisible(false);
        vbox.getChildren().add(rematch);

        // rematch buttons
        reBox = new HBox();
        reBox.setSpacing(2);
        reBox.setAlignment(Pos.CENTER);
        yes = new Button("Yes");
        yes.setPrefSize(45, 20);
        yes.setDisable(true);
        yes.setVisible(false);
        yes.setOnAction(e -> {
        	
            send(YES_SIG + id);
            rematch.setVisible(false);
            yes.setDisable(true);
            yes.setVisible(false);
            no.setDisable(true);
            no.setVisible(false);
            Platform.runLater(() -> {
                //scoreText.setText(YP_TEXT + score);
                //otherScore.setText(OP_TEXT + opScore);
                //otherPlay.setText(PLAY_TEXT);
            });

        });
        no = new Button("No");
        no.setPrefSize(45, 20);
        no.setDisable(true);
        no.setVisible(false);
        no.setOnAction(e -> stop());
        reBox.getChildren().addAll(yes, no);
        vbox.getChildren().add(reBox);

    }

    public static void main(String[] args) throws IOException{
    	launch(args);	
    }
    
    
    @Override
    public void start(Stage stage) { // display stage

        // stage
    	myStage = stage;
        //menu = new Scene(vbox, WIN_WIDTH, WIN_HEIGHT);
        //stage.setScene(menu);
        //stage.setTitle("Uno Client");
        //stage.setResizable(false);
        //stage.show();
    	
        
        BorderPane gamePane = new BorderPane();
        //set background image
        BackgroundImage myBI= new BackgroundImage(new Image(IMG_PATH+"background.jpg",600,700,false,true),
      		        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
      		          BackgroundSize.DEFAULT); //550, 680
      		
      	gamePane.setBackground(new Background(myBI));
        
        gamePane.setCenter(vbox);
        
        menu = new Scene(gamePane, WIN_WIDTH, WIN_HEIGHT);
        
        
        
        myStage.setScene(menu);
        myStage.setTitle("Uno Client");
        myStage.setResizable(false);
        
        
        
        
        myStage.show();
    }

    @Override
    public void stop() { // stop the program

        disconnect();
        System.exit(1);

    }

    // disconnect from server
    private void disconnect() {

        new Thread(() -> {

            try {

                send(NO_SIG + id);
                socket.close();
                delay();
                in.close();
                out.close();

            } catch (Exception e) {

                e.printStackTrace();

                if (e instanceof IOException) {
                    System.err.println("\nCould not disconnect from server");
                } else {
                    System.err.println("\nNot connected to server");
                }
            }

        }).start();

        //score = 0;
        //opScore = 0;

        Platform.runLater(() -> { // clear UI

            if (message.getText().toLowerCase().contains("disconnect")) {
                message.setText("Disconnected");
            }

            for (Button b : plays) {
                b.setDisable(true);
            }

        });

    }

    // initialize the buttons representing plays
    private void initButtons() {

    	ImageView view;
    	//pass = new Button();
    	
        for (int i = 0; i < NUM_PLAYS; i++) { // add buttons
            plays[i] = new Button();
            plays[i].setPrefSize(IMG_WIDTH, IMG_HEIGHT);
            view = new ImageView(images[i]);
            view.setFitWidth(IMG_WIDTH);
            view.setFitHeight(IMG_HEIGHT);
            plays[i].setGraphic(view);
            plays[i].setDisable(true);
            plays[i].setStyle("-fx-focus-color: transparent;");
            playBox.getChildren().add(plays[i]);
        }
        for (int i = 0; i < NUM_PLAYS; i++) { // set button actions
            final int j = i; // for lambda
            int x = i;
            
            //create original background image
            view = new ImageView(images[i]);
            final ImageView originalPic = view; 
            
            plays[i].setOnAction(e -> {
                new Thread(() -> {
                    for (Button b : plays) {
                        Platform.runLater(() -> b.setDisable(true));
                    }
                    
                    synchronized (this) {
                        
                    	cardPics.get(x).setStatus(true);//disable card
                    	Platform.runLater(() -> plays[x].setGraphic(originalPic) ); //set background image
                    	
                    	cardsPlayed++;		//increase the number of cards played
                    	
                    	if (cardsPlayed == 7) {
                    		send(WINNER_SIG+id);
                    		
                    	}
                    	else {
	                    	send(PLAYED_SIG+cardPics.get(x).getId());
	                    	send(PLAY_SIG); 
	                        delay();
	                        send("" + j);
                    	}
                        
                    }
                    Platform.runLater(() -> message.setText("Opponent's turn"));
                }).start();
            });
            
        }
        
        
    }

    // set connect to connect to a server
    private void setConnect() {

        connect.setText("Connect");
        connect.setOnAction(e -> {

        	
            try {
                socket = new Socket(address, port);
            } catch (Exception x) {

                x.printStackTrace();
                System.err.println("\nCould not connect to server");

            }

            try {

                listen(socket);
                send("client joined");
                setDisconnect();

            } catch (Exception x) {

                x.printStackTrace();
                System.err.println("\nUnable to read from server IO streams");

            } finally {
                System.gc();
            }

        });

    }

    // read input from server and respond
    public void listen(Socket socket) throws Exception {

        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
        new Thread(() -> {

            while (in.hasNextLine()) {

                last = in.nextLine();

                if (last.equals(WAIT_SIG)) { // connection successful; must wait for player 2

                    Platform.runLater(() -> message.setText("Waiting for opponent"));
                    id = 0;

                }
                if (last.equals(QUIT_SIG)) { // connection successful; must wait for player 2

                    
                	//myStage.close();
                	System.out.println("Quit");

                }
                else if (last.contains(CARD_SIG)) {

                	int x = Integer.parseInt(last.substring(CARD_SIG.length()) );   	
                	System.out.println("Number => "+x);  
                	cardPic card = new cardPic(x,false);
                	cardPics.add(card);				//add card photo identifier obj to array
                	cards.add( deck.idToCard(x)   ); //add card obj to cards array
                	
                }
                else if (last.equals(SCREEN_SIG)) {
                	
                	for(int i =0; i < NUM_PLAYS; i++) {
                		String x = cardPhotos.getCardPicture( cardPics.get(i).getId() );
                		Image pic = new Image(IMG_PATH + x +".png", IMG_WIDTH, IMG_HEIGHT, false, true);
                		System.out.println(IMG_PATH+x+".png");
                		ImageView img = new ImageView(pic);
                		int index = i;
                		Platform.runLater(() -> plays[index].setGraphic(img) );
                	}
                	
                }
                else if (last.contains(PLAYED_SIG)) {
                	int y = Integer.parseInt(last.substring(PLAYED_SIG.length()) );   
                	String x = cardPhotos.getCardPicture( y );
                	Image pic = new Image(IMG_PATH + x +".png", 150, 200, false, true);
                	//ImageView img = new ImageView(pic);
                	Platform.runLater(() -> mainImage.setImage(pic) );
                	
                	//Add played card to determine which cards can be played
                	timesPassed = 0;
                	cardOnTop = deck.idToCard(y);
                
                }
                else if (last.equals(PASS_SIG)) {
                	
                	timesPassed++; //increase pass counter
                	
                	if(timesPassed == 4) {
                		
                		send(TIE_SIG);
                		
                	}
                	
                	System.out.println(timesPassed);
                }
                else if (last.contains(UPDATE_SIG)) {//update list of clients
                	int x = Integer.parseInt(last.substring(UPDATE_SIG.length()));
                	clientList.add(x);
                	//opponents.setItems(clientList);
                }
                else if (last.equals(CLEAR_SIG)) { // client's turn; enable play buttons
                	
                	clientList.clear();
                
                }
                //else if (last.equals(DISABLE_SIG)) {
                	
                	//pass.setDisable(true);
                	//opponents.setDisable(true);
                	
                //}
                else if (last.equals(REJECT_SIG)) {
                	message.setText("Player in Game!");
                }
                else if (last.contains(WEL_SIG)) { // connection successful

                    Platform.runLater(() -> message.setText("Connected") );
                    Platform.runLater(() -> message.setStyle("-fx-text-fill: blue") );
                    Platform.runLater(() -> message.setFont(Font.font("Verdana", FontWeight.BOLD, 18)) );
                    
                    id = Integer.parseInt(last.substring(WEL_SIG.length()));
		    Platform.runLater(()-> ID.setText("ID: "+ id));

                    if (id == 4) {
                    	send(CHALLENGE_SIG);
                    }
                   
                }
                else if (last.equals(TURN_SIG)) { // client's turn; enable play buttons

                    Platform.runLater(() -> {

                        message.setText("Your turn");
                        pass.setDisable(true);
                        //opponents.setDisable(true);

                        //for (Button b : plays) { 
                        	//b.setDisable(false);
                        //}
                        
                        boolean passTurn = true;
                        
                        //enable buttons that have not been played
                        for(int i = 0; i < cards.size(); i++) {
                        	
                        	//enable cards that can be played
                        	
                        	//no one has played
                        	if (cardOnTop.getId() == -1) {
                        		plays[i].setDisable(false);
                        		passTurn = false;
                        	}
                        	else {
                        	
	                        	System.out.println(cardOnTop.getNumber() + ": " + cardOnTop.getColor());
	                        	
	                        	//number or color is the same)
	                        	if(  cards.get(i).getNumber() == cardOnTop.getNumber() || cards.get(i).getColor() == cardOnTop.getColor()  ) {
	                        		//card has not been played
	                        		if (cardPics.get(i).getStatus() == false) {
	                        			plays[i].setDisable(false);
	                        			passTurn = false; //disable pass
	                        		}                        		
	                        	}
                        	}
                        	
                        }
                        
                        if (passTurn == true) {
                        	pass.setDisable(false);
                        }
                    }); 

                }

                else if (last.equals(WIN_SIG)) {

                    //score++;
                    Platform.runLater(() -> message.setText("You won!"));
                    System.out.println("WON!");
                    Platform.runLater(() -> message.setStyle("-fx-text-fill: green") );

                }

                else if (last.equals(LOSE_SIG)) {

                    //opScore++;
                    Platform.runLater(() -> message.setText("You lost!"));
                    Platform.runLater(() -> message.setStyle("-fx-text-fill: red") );

                }

                else if (last.equals(TIE_SIG)) {
                    Platform.runLater(() -> message.setText("You tied!"));
                    Platform.runLater(() -> message.setStyle("-fx-text-fill: blue") );
                }

                else if (last.contains(OP_SIG)) {
                   // Platform.runLater(() -> otherPlay.setText(PLAY_TEXT + last.substring(OP_SIG.length())));
                }

                else if (last.equals(END_SIG)) {

                    //if (score > opScore) {
                     //   Platform.runLater(() -> message.setText("You won the game! Rematch?"));
                    //} else {
                     //   Platform.runLater(() -> message.setText("You lost the game! Rematch?"));
                    //}

                    Platform.runLater(() -> {

                        rematch.setVisible(true);
                        yes.setDisable(false);
                        yes.setVisible(true);
                        no.setDisable(false);
                        no.setVisible(true);

                    });

                }

                else if (last.contains(FORCE_SIG)) {

                    int source = Integer.parseInt(last.substring(FORCE_SIG.length()));
                    disconnect();
                    Platform.runLater(() -> {

                        setConnect();
                        rematch.setVisible(false);
                        yes.setDisable(true);
                        yes.setVisible(false);
                        no.setDisable(true);
                        no.setVisible(false);

                        switch (source) {

                            case -1 : 	message.setText("Server disconnect"); 
                            			myStage.close();
                            			break;
                            			
                            case 0  : message.setText("Player 1 quit, disconnecting"); break;
                            case 1  : message.setText("Player 2 quit, disconnecting"); break;

                        }

                    });

                }

            }

        }).start();

    }

    // set connect to disconnect from a server
    private void setDisconnect() {

        connect.setText("Disconnect");
        connect.setOnAction(e -> {

            disconnect();
            setConnect();

        });

    }
     

    // send a signal to the server
    private void send(String message) {

        out.println(message);
        out.flush();

    }

    // wait for a tenth of a second
    private void delay() {

        try {
            Thread.sleep(100);
        } catch (InterruptedException x) {
            x.printStackTrace();
        }

    }

}


