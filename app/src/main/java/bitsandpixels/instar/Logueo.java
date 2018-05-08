package bitsandpixels.instar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Logueo extends AppCompatActivity {

    private EditText pLogueoEmail;
    private EditText pLogueoClave;
    private Button pLogueoBoton;
    private Button pRegistrarseBoton;

    private FirebaseAuth Autenticacion;

    private ProgressDialog pProgreso;

    private DatabaseReference pDatabaseUsuarios;

    private SignInButton pGoogleBoton;

    private  static final  int RC_SIGN_IN = 1;

    private  static final String TAG = "Logueo";

    private GoogleApiClient PGoogleApiCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logueo);

        Autenticacion = FirebaseAuth.getInstance();

        pDatabaseUsuarios = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        pDatabaseUsuarios.keepSynced(true);

        pProgreso = new ProgressDialog(this);

        pGoogleBoton = (SignInButton) findViewById(R.id.BotonGoogle);

        pLogueoEmail = (EditText) findViewById(R.id.LogueoEmail);
        pLogueoClave = (EditText) findViewById(R.id.LogueoClave);

        pLogueoBoton = (Button) findViewById(R.id.LogueoBoton);

        pLogueoBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin();
            }
        });

        pRegistrarseBoton = (Button) findViewById(R.id.RegistrarseBoton);

        pRegistrarseBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainnIntent = new Intent(Logueo.this, Registro.class);
                mainnIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainnIntent);
            }
        });

        // ----------- GOOGLE INICIO DE SESION ----------- //
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        PGoogleApiCliente = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        pGoogleBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn();

            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(PGoogleApiCliente);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            pProgreso.setMessage("Iniciando Sesion ...");
            pProgreso.show();

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                pProgreso.dismiss();

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Autenticacion.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(Logueo.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            pProgreso.dismiss();
                            ChequeoExistencia_de_Usuario();
                        }

                        // ...
                    }
                });
    }


    private void checkLogin() {
        String Email = pLogueoEmail.getText().toString().trim();
        String Clave = pLogueoClave.getText().toString().trim();

        if(!TextUtils.isEmpty(Email) && !TextUtils.isEmpty(Clave)){

            pProgreso.setMessage("Iniciando Sesion ...");
            pProgreso.show();

            Autenticacion.signInWithEmailAndPassword(Email,Clave).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        pProgreso.dismiss();

                        ChequeoExistencia_de_Usuario();

                    } else {

                        pProgreso.dismiss();

                        Toast.makeText(Logueo.this,"Usuario o Clave Incorrecto",Toast.LENGTH_LONG).show();

                    }
                }
            });
        }


    }

    private void ChequeoExistencia_de_Usuario() {

        if(Autenticacion.getCurrentUser() != null) {

            final String ID_Usuario = Autenticacion.getCurrentUser().getUid();

            pDatabaseUsuarios.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild(ID_Usuario)) {

                        Intent mainIntent = new Intent(Logueo.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);

                    } else {

                        //  Toast.makeText(Logueo.this,"Necesitas crear una cuenta ...",Toast.LENGTH_LONG).show();

                        Intent ConfigIntent = new Intent(Logueo.this, Configuracion.class);
                        ConfigIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(ConfigIntent);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
}
