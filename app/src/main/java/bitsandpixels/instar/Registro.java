package bitsandpixels.instar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registro extends AppCompatActivity {

    private EditText CampoSeudonimo;
    private EditText CampoEmail;
    private EditText CampoPass;

    private Button BotonRegistro;

    private FirebaseAuth pAtutenticacion;
    private DatabaseReference pBasedeDato;

    private ProgressDialog pProgreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        pAtutenticacion = FirebaseAuth.getInstance();

        pBasedeDato = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        pProgreso = new ProgressDialog(this);

        CampoSeudonimo = (EditText) findViewById(R.id.Seudonimo);
        CampoEmail = (EditText) findViewById(R.id.Email);
        CampoPass = (EditText) findViewById(R.id.Clave);
        BotonRegistro = (Button) findViewById(R.id.Registrar);

        BotonRegistro.setOnClickListener(new View.OnClickListener(){
            @Override
                    public void onClick (View view){
                ComienzoRegistro();
            }
        });

    }

    private void ComienzoRegistro() {

        final String Seudonimo = CampoSeudonimo.getText().toString().trim();
        String Email = CampoEmail.getText().toString().trim();
        String Contrasena = CampoPass.getText().toString().trim();

        if(!TextUtils.isEmpty(Seudonimo) && !TextUtils.isEmpty(Contrasena)){

            pProgreso.setMessage("Registrando Usuario...");
            pProgreso.show();

            pAtutenticacion.createUserWithEmailAndPassword(Email,Contrasena).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        String ID_Usuario = pAtutenticacion.getCurrentUser().getUid();

                        DatabaseReference ID_Usuario_Actual = pBasedeDato.child(ID_Usuario);

                        ID_Usuario_Actual.child("Seudonimo").setValue(Seudonimo);
                        ID_Usuario_Actual.child("Imagen").setValue("https://firebasestorage.googleapis.com/v0/b/data-r.appspot.com/o/Foto_Perfil%2Fdefault_profile.png?alt=media&token=cdb89942-a8c5-473f-bf79-454771c03b68");

                        pProgreso.dismiss();

                        Intent MainIntent = new Intent(Registro.this,MainActivity.class);
                        MainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(MainIntent);
                    }
                }
            });
        }
    }
}
