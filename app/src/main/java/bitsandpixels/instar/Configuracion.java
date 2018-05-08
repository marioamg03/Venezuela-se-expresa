package bitsandpixels.instar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class Configuracion extends AppCompatActivity {

    private ImageButton pImagenConfig;
    private EditText pNombreConfig;
    private Button pBotonConfig;

    private Uri pImagenUri = null;

    private DatabaseReference pDatabaseUsuarios;

    private FirebaseAuth pAutenticacion;

    private StorageReference pAlmacenamientoPerfil;

    private ProgressDialog pActualizando;

    private  static final int GALERIA_PETICION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        pAutenticacion = FirebaseAuth.getInstance();

        pAlmacenamientoPerfil = FirebaseStorage.getInstance().getReference().child("Foto_Perfil");

        pDatabaseUsuarios = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        pActualizando =new ProgressDialog(this);

        pImagenConfig = (ImageButton) findViewById(R.id.ConfigImagen);
        pNombreConfig = (EditText) findViewById(R.id.ConfigNombre);
        pBotonConfig = (Button) findViewById(R.id.GuardarCambios);

        pBotonConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComienzoConfigCuenta();
            }
        });


        pImagenConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent peticiongaleria = new Intent();
                peticiongaleria.setAction(Intent.ACTION_GET_CONTENT);
                peticiongaleria.setType("image/*");
                startActivityForResult(peticiongaleria,GALERIA_PETICION);

            }
        });
    }

    private void ComienzoConfigCuenta() {

        final String nombre = pNombreConfig.getText().toString().trim();

        final String ID_USUARIO = pAutenticacion.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(nombre) && pImagenUri!=null){

            pActualizando.setMessage("Finalizando la configuracion ...");
            pActualizando.show();

            StorageReference Ruta_Archivo = pAlmacenamientoPerfil.child(pImagenUri.getLastPathSegment());
            Ruta_Archivo.putFile(pImagenUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String DescargaUri =taskSnapshot.getDownloadUrl().toString();

                    pDatabaseUsuarios.child(ID_USUARIO).child("Seudonimo").setValue(nombre);
                    pDatabaseUsuarios.child(ID_USUARIO).child("Imagen").setValue(DescargaUri);

                    pActualizando.dismiss();

                    Intent mainIntent = new Intent(Configuracion.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });

        }


    }

    @Override
    protected void onActivityResult(int resquestCode, int resultCode, Intent data){
        super.onActivityResult(resquestCode,resultCode,data);

        if( resquestCode == GALERIA_PETICION && resultCode == RESULT_OK) {

            Uri Imagenuri = data.getData();

            // Crop Original!
            CropImage.activity(Imagenuri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setAllowRotation(true)
                    .setActivityTitle("Perfil")
                    .setAllowFlipping(false)
                    .start(this);
        }

        // Crop Original!
        if (resquestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                pImagenUri = result.getUri();

                pImagenConfig.setImageURI(pImagenUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }



    }




}
