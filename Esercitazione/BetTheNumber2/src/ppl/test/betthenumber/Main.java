package ppl.test.betthenumber;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements MessageReceiver {
	
	protected static final int SHOW_TOAST = 0;
	public String TAG = "ppl.indovinailnumero.main";
	
	
	enum Stato {
		WAIT_FOR_START,WAIT_FOR_START_ACK,USER_SELECTING,WAIT_FOR_NUMBER_SELECTION,WAIT_FOR_BET,USER_BETTING
	}

	
	private TextView toplabel;
	ConnectionManager connection;
	private Stato statoCorrente;
	
	
	Timer timer = new Timer();
	TimerTask sendStart = new TimerTask() {

		@Override
		public void run() {
			
		}
	};
	
	
	
	
	final Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Main.SHOW_TOAST:
				Toast.makeText(Main.this,msg.getData().getString("toast"), Toast.LENGTH_LONG).show();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};
	private String selectedNumber;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		String nomeProprio = getIntent().getExtras().getString("NOMEPROP");
		String nomeAvversario = getIntent().getExtras().getString("NOMEAVV");
		
		toplabel = (TextView) findViewById(R.id.topLabel);
		toplabel.setText(nomeProprio+":"+nomeAvversario);
	    connection = new ConnectionManager(nomeProprio, nomeAvversario, this);
	    
	    if(nomeAvversario.hashCode()<nomeProprio.hashCode()){
	    	// Inizio io
	    	timer.schedule(sendStart, 1000, 5000);
	    	statoCorrente=Stato.WAIT_FOR_START_ACK;
	    } else{
	    	// Inizia lui
	    	//Io aspetto il pacchetto;
	    	statoCorrente=Stato.WAIT_FOR_START;
	    }
	    
	}


	public void receiveMessage(String body) {
		if (body.equals("START")) {
			if (statoCorrente == Stato.WAIT_FOR_START) {
				// Mando l'ack indietro
				connection.send("STARTACK");
				Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
				Bundle b = new Bundle();
				b.putString("toast", "Scegli un numero");
				osmsg.setData(b);
				handler.sendMessage(osmsg);
				statoCorrente=Stato.USER_SELECTING;
			} else {
				Log.e(TAG, "Ricevuto START ma los stato  " + statoCorrente);
			}
		} else if (body.equals("STARTACK")) {
			if (statoCorrente == Stato.WAIT_FOR_START_ACK) {
				statoCorrente=Stato.WAIT_FOR_NUMBER_SELECTION;
			} else {
				Log.e(TAG, "Ricevuto STARTACK ma los stato  " + statoCorrente);
			}
		} else if (body.startsWith("SELECTED")) {
			if (statoCorrente == Stato.WAIT_FOR_NUMBER_SELECTION){
				selectedNumber = body.split(":")[1];
				Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
				Bundle b = new Bundle();
				b.putString("toast", "Indovina il numero");
				osmsg.setData(b);
				handler.sendMessage(osmsg);
				statoCorrente=Stato.USER_BETTING;
			}else{
				Log.e(TAG, "Ricevuto SELECTED ma los stato  " + statoCorrente);
			}
			
		} else if (body.startsWith("BET")) {
			if (statoCorrente == Stato.WAIT_FOR_BET){
				String result  = body.split(":")[1];
				Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
				Bundle b = new Bundle();
				if(result.equals("Y"))
					b.putString("toast", "Hai perso, il tuo avversario ha indovinato");
				else
					b.putString("toast", "Hai vinto, il tuo avversario ha sbagliato");
				osmsg.setData(b);
				handler.sendMessage(osmsg);
				statoCorrente=Stato.WAIT_FOR_NUMBER_SELECTION;
			}else{
				Log.e(TAG, "Ricevuto SELECTED ma los stato  " + statoCorrente);
			}
			
		}
		
	}
	
	
	public void numberSelected(View v){
		Button b = (Button) v;
		
		if (statoCorrente == Stato.USER_SELECTING) {
			connection.send("SELECTED:"+b.getText().toString());
			statoCorrente=Stato.WAIT_FOR_BET;
		} else if (statoCorrente == Stato.USER_BETTING) {
			String bet = b.getText().toString();
			if(bet.equals(selectedNumber)){
				connection.send("BET:Y");
				Toast.makeText(Main.this,"Bravo hai indovinato, ora tocca te", Toast.LENGTH_LONG).show();
			} else{
				connection.send("BET:N");
				Toast.makeText(Main.this,"Peccato non hai indovinato, ora tocca te", Toast.LENGTH_LONG).show();
			}
			statoCorrente=Stato.USER_SELECTING;
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		connection.close();
		timer.cancel();
	}
	
}
