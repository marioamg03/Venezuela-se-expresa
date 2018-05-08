package bitsandpixels.instar;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class onepost extends AppCompatActivity {

    private RecyclerView PostLista;
    private DatabaseReference pDatabase;

    private static DatabaseReference pDatabaseUsuarios;

    private DatabaseReference pDatabaseRef;
    private Query pQueryEstado;

    private FirebaseAuth pAutenticacion;
    private FirebaseAuth.AuthStateListener pAutenticacionOyente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onepost);

        pAutenticacion = FirebaseAuth.getInstance();
        pAutenticacionOyente = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {

                    Intent loginIntent = new Intent(onepost.this, Logueo.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
            }
        };


        //Me ubico en el contenidod e los posts los cuales se encuentran en la llave
        //'Principal' de la base de datos
        pDatabase = FirebaseDatabase.getInstance().getReference().child("Principal");
        pDatabaseUsuarios = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        String Valor ="";
        Valor = getIntent().getExtras().getString("Valor");

        // Utilizo la variable estado para obtener los datos previos en el activity

        if (Valor.equals("VSE")){
            // Ordeno la base de datos por la referencia que estableci antes.
            pDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Principal");
            pQueryEstado = pDatabaseRef.orderByChild("ID_U").equalTo("Etx9bNGhqpaRP06nSPcTIqpN8RM2");
        } else{
            // Ordeno la base de datos por la referencia que estableci antes.
            pDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Principal");
            pQueryEstado = pDatabaseRef.orderByChild("Titulo").equalTo(Valor);
        }

        //Mantengo sincronizado el inicio de sesion
        pDatabaseUsuarios.keepSynced(true);

        //Mantengo sincronizado el contenido de la app.
        pDatabase.keepSynced(true);

        //Lista de publicaciones en el menu principal
        PostLista = (RecyclerView) findViewById(R.id.ListaDePost);

        // Creo un layout lineal de contenido y lo volveo para conseguir las
        // noticias ma reciente en la parte de arriba
        LinearLayoutManager pLayoutManager = new LinearLayoutManager(this);
        pLayoutManager.setReverseLayout(true);
        pLayoutManager.setStackFromEnd(true);

        PostLista.setHasFixedSize(true);
        PostLista.setLayoutManager(pLayoutManager);

        ChequeoExistencia_de_Usuario();

    }





    @Override
    protected void onStart(){
        super.onStart();

        pAutenticacion.addAuthStateListener(pAutenticacionOyente);

        FirebaseRecyclerAdapter<Principal,onepost.PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Principal, onepost.PostViewHolder>(
                Principal.class,
                R.layout.filas_post,
                onepost.PostViewHolder.class,
                pQueryEstado
        ){
            @Override
            protected void populateViewHolder(onepost.PostViewHolder viewHolder, Principal model, int position) {

                viewHolder.AsignoTitulo(model.getTitulo());
                viewHolder.AsignoContenido(model.getContenido());
                viewHolder.AsignoID_U(model.getID_U());
                viewHolder.AsignoImagenP(getApplicationContext(),model.getID_U());
                viewHolder.AsignoImagen(getApplicationContext(),model.getImagen());
                viewHolder.AsignoVideo(getApplicationContext(),model.getVideo());
            }
        };

        PostLista.setAdapter(firebaseRecyclerAdapter);
    }

    private void ChequeoExistencia_de_Usuario() {

        if (pAutenticacion.getCurrentUser() != null) {

            final String ID_Usuario = pAutenticacion.getCurrentUser().getUid();

            pDatabaseUsuarios.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(ID_Usuario)) {

                        Intent ConfigIntent = new Intent(onepost.this, Configuracion.class);
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

    public static  class PostViewHolder extends RecyclerView.ViewHolder{
        View pView;
        public  PostViewHolder (View itemView){
            super(itemView);
            pView = itemView;
        }
        //Asigno el titulo del post(Estado)
        public  void  AsignoTitulo(String Titulo){
            TextView Titulo_Post = (TextView) pView.findViewById(R.id.post_titulo);
            Titulo_Post.setText(Titulo);
        }
        //Asigno el Contenido del post
        public  void  AsignoContenido(String Contenido){
            TextView Contenido_Post = (TextView) pView.findViewById(R.id.post_contenido);
            Contenido_Post.setText(Contenido);
        }
        //Asignar con el ID de usuario el nombre en el post
        public  void  AsignoID_U(String ID_U){
            final TextView ID_U_Post = (TextView) pView.findViewById(R.id.post_nombreusuario);
            pDatabaseUsuarios.child(ID_U).child("Seudonimo").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String Nombre = (String) dataSnapshot.getValue();
                    ID_U_Post.setText(Nombre);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        //Asigno la imagen del perfil
        public  void AsignoImagenP(final Context ctx , final String ID_U){

            final CircularImageView circularImageView = (CircularImageView) pView.findViewById(R.id.Profile);

            pDatabaseUsuarios.child(ID_U).child("Imagen").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String Imagen = (String) dataSnapshot.getValue();

                    Picasso.with(ctx).load(Imagen).networkPolicy(NetworkPolicy.OFFLINE).into(circularImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(Imagen).into(circularImageView);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }
        // Asigno la imagen del Post
        public  void AsignoImagen(final Context ctx , final String Imagen){
            final ImageView Imagen_Post = (ImageView) pView.findViewById(R.id.post_imagen);

            //Picasso.with(ctx).load(Imagen).into(Imagen_Post);
            Picasso.with(ctx).load(Imagen).networkPolicy(NetworkPolicy.OFFLINE).into(Imagen_Post, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(ctx).load(Imagen).into(Imagen_Post);

                }
            });

        }
        // Asigno la imagen del Post
        public  void AsignoVideo(final Context ctx ,final String Video){
            if(Video!=null){
                JCVideoPlayerStandard Video_Post = (JCVideoPlayerStandard) pView.findViewById(R.id.post_video);
                Video_Post.setUp(Video , JCVideoPlayerStandard.CURRENT_STATE_NORMAL,"");
                Video_Post.setVisibility(pView.VISIBLE);
            }
        }


    }


}
