package ppl.test.betthenumber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Start extends Activity {
    private EditText nomeProprio, nomeAvversario;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        nomeProprio = (EditText) findViewById(R.id.nomeproprio);
        nomeAvversario = (EditText) findViewById(R.id.nomeAvversario);
        
    }
    
    
    
    public void play(View v){
    	Intent startIntent = new Intent(this, Main.class);
		String tmp = nomeProprio.getText().toString();
		startIntent.putExtra("NOMEPROP",tmp);
		tmp = nomeAvversario.getText().toString();
		startIntent.putExtra("NOMEAVV",tmp);
		startActivity(startIntent);
    	
    }
}