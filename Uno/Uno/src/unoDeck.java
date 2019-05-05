import java.util.ArrayList;
import java.util.Collections;

public class unoDeck {

	private ArrayList<unoCard> deck;
	
	public unoDeck (){
	
		deck = new ArrayList<unoCard>();
		
		int counter = 0;
		
		for(int i = 0; i <= 39; i++) {
			
			unoCard card = new unoCard();
			cardImages images = new cardImages();
			
			card.setId(i); //add id
			
			card.setImage( images.getCardPicture(i) ); //add image
			
			card.setNumber(counter); //add number
			
			//initiate card color
			if (i < 10) {
				card.setColor("Blue");
				counter = 0;
			}
			else if (i > 9 || i < 20) {
				card.setColor("Green");
				counter = 0;
			}
			else if (i > 19 || i < 30) {
				card.setColor("Yellow");
				counter = 0;
			}
			else if (i > 29 || i < 40) {
				card.setColor("Red");
				counter = 0;
			}
			
			//add card
			deck.add(card);
			
			counter++;
	
		}
		
	}
	
	public void shuffle() {
		Collections.shuffle(deck);
	}
	
	public ArrayList<unoCard> dealHand(int num){
		
		ArrayList<unoCard> hand = new ArrayList<unoCard>();
		
		for(int i = 0; i<7; i++) {
			hand.add(deck.get(i));
			deck.remove(i);
		}
		
		return hand;
	}
}
