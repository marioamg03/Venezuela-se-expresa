package bitsandpixels.instar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;

public class PostActivity extends Fragment {

    private ImageButton pImagen;
    private EditText pContenido;

    private Uri pImagenUri = null;

    private Button pEnviar;

    private  static  final int GALLERY_RESQUEST = 1;

    private StorageReference RefAlm;
    private DatabaseReference RefDatos;

    private ProgressDialog Pprogreso;

    private FirebaseAuth pAutenticacion;

    private FirebaseUser pUsuarioActual;

    private DatabaseReference pDatabaseUsuarios;

    AutoCompleteTextView pEstados;
    String item[]={
            "Amazonas", "Anzoátegui", "Apure", "Aragua",
            "Barinas", "Bolívar", "Carabobo", "Cojedes",
            "Delta Amacuro", "Distrito Capital", "Falcón", "Guárico",
            "Lara", "Mérida", "Miranda", "Monagas",
            "Nueva Esparta", "Portuguesa", "Sucre", "Táchira",
            "Trujillo", "Vargas", "Yaracuy", "Zulia"};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_post, container, false);

        pAutenticacion = FirebaseAuth.getInstance();
        pUsuarioActual = pAutenticacion.getCurrentUser();

        RefAlm = FirebaseStorage.getInstance().getReference();
        RefDatos = FirebaseDatabase.getInstance().getReference().child("Principal");

        pDatabaseUsuarios = FirebaseDatabase.getInstance().getReference().child("Usuarios").child(pUsuarioActual.getUid());

        pImagen = (ImageButton) rootView.findViewById(R.id.imagenpost);

        pContenido = (EditText) rootView.findViewById(R.id.descripcionpost);
        pEnviar = (Button) rootView.findViewById(R.id.publicarpost);

        Pprogreso = new ProgressDialog(getActivity());

        // Obtencion de la imagen en el Almacenamiento interno del telefono
        pImagen.setOnClickListener(new View.OnClickListener(){
         @Override
                 public void onClick(View view){
             Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
             galleryIntent.setType("image/*");
             startActivityForResult(galleryIntent, GALLERY_RESQUEST);
            }
        });

        pEnviar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                EmpiezaPublicacion();
            }
        });

        pEstados = (AutoCompleteTextView)rootView.findViewById(R.id.autoCompleteEstado);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplication(), android.R.layout.simple_expandable_list_item_1, item);
        pEstados.setAdapter(adapter);

        return rootView;

    }


    //Comienzo a subir los archivos y a obtener los elementos para la base de datos
    private void  EmpiezaPublicacion() {

        Pprogreso.setMessage("Publicando ...");
        Pprogreso.show();

        final String val_estado = pEstados.getText().toString().trim();
        final String val_contenido = pContenido.getText().toString().trim();

        //ImagenP.getLastPathSegment() Es el nombre con el cual se va a subir la imagen a el Almacenamiento
        // Ahi se puede reemplazar por un codigo generador de String para que no existan ninguno igual

        if(!TextUtils.isEmpty(val_estado) && (!TextUtils.isEmpty(val_contenido)) && pImagenUri != null){

            StorageReference rutaArch = RefAlm.child("Imagen_Public").child(pImagenUri.getLastPathSegment());

            rutaArch.putFile(pImagenUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                   final Uri descURL = taskSnapshot.getDownloadUrl();

                    final DatabaseReference Nuevo_Post = RefDatos.push();

                    pDatabaseUsuarios.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // Mas adelante tengo que cambiar este Titulo por la Seccion de los estados.
                            Nuevo_Post.child("Titulo").setValue(val_estado);

                            // Esta es la descripcion del post (deberia limitarla?)
                            Nuevo_Post.child("Contenido").setValue(val_contenido);

                            //El enlace que genero con la ruta de la imagen
                            Nuevo_Post.child("Imagen").setValue(descURL.toString());

                            // Esta es la la ID del usuario que postea
                            Nuevo_Post.child("ID_U").setValue(pUsuarioActual.getUid());

                            // Este es el enlace de video pero como es una imagen no coloco nada.
                            Nuevo_Post.child("Video").setValue("");

                            //Este es el Nombre de usurio que postea
                            Nuevo_Post.child("Usuario").setValue(dataSnapshot.child("Seudonimo").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        // Luego que hago la publicacion de el Post me regreso a mi pantalla principal
                                        startActivity(new Intent(getActivity(), MainActivity.class));

                                    }
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });

                    Pprogreso.dismiss();
                }
            });
        }
    }

    //Este codigo es el Croop de la imagen a publicar
    @Override
    public   void onActivityResult (int resquestCode, int resultCode, Intent data){
        super.onActivityResult(resquestCode,resultCode,data);
        if( resquestCode == GALLERY_RESQUEST && resultCode == RESULT_OK) {

            Uri Imagenuri = data.getData();

            // Crop Original!
            CropImage.activity(Imagenuri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .setAllowRotation(true)
                    .setActivityTitle("Recortar")
                    .setAllowFlipping(false)
                    .start(getContext(),this);
            // no se utiliza el getactivity() segun la documentacion
        }

        // Crop Original!
        if (resquestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                pImagenUri = result.getUri();

                pImagen.setImageURI(pImagenUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
