package view;

import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import client.ClientController;
import client.Message;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import server.ServerController;

public class ChatView extends Stage{
	

	private Label lblChatInfo = new Label();
	
	
	private Label lblRecipientName = new Label("Tên người nhận:");
	private TextField tfRecipient = new TextField();
	private ComboBox<String> cbRecipient = new ComboBox<>();
	private Button btnChangeRecipient = new Button("Chat ngay");
	private Label lblStatus = new Label("Hiện tại chưa thể nhận tin nhắn");

	private String encryptionKey = "SECRET_KEY";
	private ListView<Text> chatWindow = new ListView<>();
	
	private TextField tfInput = new TextField();
	private Button btnChat = new Button("Gửi");
	
	private Label lblNotification = new Label();
	private Button btnDismissNotification = new Button("X");
	
	private BorderPane bp = new BorderPane();
	
	private ClientController controller = ClientController.getInstance();

	public ChatView() {
		
		
		
		// top 
		HBox hbTop = new HBox(10);
		hbTop.setPadding(new Insets(15, 10, 5, 10));
		hbTop.getChildren().addAll(lblChatInfo,lblRecipientName,cbRecipient,btnChangeRecipient,lblStatus);
		hbTop.setAlignment(Pos.CENTER);
		// end top
		chatWindow.setStyle("-fx-background-color: #ADD8E6;");
		
		// center 
		for (int i = 0; i < 20; i++) {
			//chatWindow.getItems().add(" Item "+i);
			
		}
		//chatWindow.getItems().addAll("test","test2");		
		// end center
		
		
		// bottom
		HBox hbBottom = new HBox(10);
		hbBottom.setPadding(new Insets(5,5,10,5));
		hbBottom.setAlignment(Pos.CENTER_LEFT);
		hbBottom.getChildren().addAll(tfInput,btnChat);
		
		tfInput.setPrefWidth(720);
		tfInput.setPrefHeight(35);
		
		btnChat.setPrefHeight(35);	
		// end bottom
		
		this.btnDismissNotification.setVisible(false);
		HBox hbNotification = new HBox(15);
		hbNotification.setAlignment(Pos.CENTER);
		hbNotification.setPadding(new Insets(5, 5, 0, 5)); 
		hbNotification.getChildren().addAll(lblNotification,btnDismissNotification);
	
		VBox vbBottom = new VBox();
		vbBottom.setAlignment(Pos.CENTER);
		vbBottom.setPadding(new Insets(5, 5, 0, 5)); 	// top, right, bottom, left
		vbBottom.getChildren().addAll(hbNotification,hbBottom);
		
		bp.setTop(hbTop);
		bp.setCenter(chatWindow);
		bp.setBottom(vbBottom);

		Scene sc = new Scene(bp,800,500);
		this.setScene(sc);

		this.setActions();
		this.setResizable(false);
		
	}
	
	
	private void setActions() {
		this.btnChat.setOnAction(e -> {
			String chatText = this.tfInput.getText();
			
			if (chatText.contains(";")) {
				this.addAlert("Kí tự không hợp lệ","[ ; ] được sử dụng làm dấu phân cách và không được phép có trong tin nhắn");
				return;
			}
			String recipient = this.cbRecipient.getSelectionModel().getSelectedItem();
			if (recipient.isEmpty()) {
				this.addAlert("Không có người nhận nào được chọn","Để gửi tin nhắn bạn phải chọn người nhận");
				return;
			}
			
			Message msg = new Message(chatText, this.controller.getUsername());
			
			this.addMessage(msg,true);
			this.scrollToBottom();
			this.tfInput.setText("");
			
			try {
				this.controller.sendMessage(msg,recipient);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		this.btnChangeRecipient.setOnAction(e -> {
			String newRecipient = this.cbRecipient.getSelectionModel().getSelectedItem();
			ClientController controller = ClientController.getInstance();
			controller.setRecipient(newRecipient);
			System.out.println("New recipient is "+controller.getRecipient());
			this.chatWindow.getItems().clear();
			this.chatWindow.refresh();
			
			if (!newRecipient.isEmpty()) {
				this.lblStatus.setText("Bạn có thể nhận tin nhắn từ "+newRecipient);
			} else {
				this.lblStatus.setText("Hiện tại không thể nhận tin nhắn nào");
			}
			
		});
		
		bp.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
	        if (e.getCode() == KeyCode.ENTER) {
	           this.btnChat.fire();
	           e.consume(); 
	        }
	    });
		
		this.btnDismissNotification.setOnAction(e -> {
			this.lblNotification.setText("");
			this.btnDismissNotification.setVisible(false);
		});
		
	}

	public void addAlert(String title, String description) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setHeaderText(title);
		alert.setContentText(description);

		alert.showAndWait();
	}
	
	public void loadData() {
		this.lblChatInfo.setText("Đăng nhập với tư cách: " + this.controller.getUsername());
	}
	private String decryptMessage(String encryptedMessage) {
		StringBuilder decryptedText = new StringBuilder();

		// Split the message into username and actual message
		String[] parts = encryptedMessage.split(":", 2);

		if (parts.length == 2) {
			String username = parts[0].trim();
			String message = parts[1].trim();

			for (int i = 0; i < message.length(); i++) {
				char currentChar = message.charAt(i);
				char decryptedChar;

				if (Character.isLetter(currentChar)) {
					decryptedChar = (char) (currentChar - encryptionKey.length());

					// Wrap around for characters outside the alphabet
					if (Character.isUpperCase(currentChar) && decryptedChar < 'A') {
						decryptedChar = (char) (decryptedChar + 26);
					} else if (Character.isLowerCase(currentChar) && decryptedChar < 'a') {
						decryptedChar = (char) (decryptedChar + 26);
					}
				} else {
					decryptedChar = currentChar;
				}

				decryptedText.append(decryptedChar);
			}

			return username + ": " + decryptedText.toString();
		} else {
			return encryptedMessage;
		}
	}
	public void addMessage(Message msg, boolean bold) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				String encryptedData = msg.getData();
				System.out.println("Encrypted Data: " + encryptedData);

				String decryptedMessage = decryptMessage(encryptedData);
				System.out.println("Decrypted Message: " + decryptedMessage);
				String tin = "[" + msg.getTimestamp() + "] - " + msg.getSender() + ": " + msg.getData();
				Text txt = new Text(tin);
				System.out.println("Final Text: " + txt.getText());

				if (bold) {
					txt.setFont(Font.font("Verdana", FontWeight.EXTRA_LIGHT, 12));
				} else {
					txt.setFont(Font.font("Verdana", FontWeight.NORMAL, 13));
				}

				chatWindow.getItems().add(txt);
				chatWindow.refresh();
				scrollToBottom();
			};


		});

	}
	
	public void addUserToListOfOnlineUsers(String user) {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {	
				cbRecipient.getItems().add(user);
			};
				
		});
	}
	public void removeUserFromListOfOnlineUsers(String user) {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {	
				cbRecipient.getItems().remove(user);
			};	
		});
	}
	
	public void setNotificationText(String msg) {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {	
				btnDismissNotification.setVisible(true);
				lblNotification.setText(msg);
			};
				
		});		
	}
	
	
	
	public void updateTitle() {
		this.setTitle("Chat window ["+controller.getUsername()+"]");
	}
	
	private void scrollToBottom() {
		this.chatWindow.scrollTo(this.chatWindow.getItems().size()-1);
	}
	
	
	
	
}
