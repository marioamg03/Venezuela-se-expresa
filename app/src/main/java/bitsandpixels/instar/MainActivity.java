package bitsandpixels.instar;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageButton BuscarEstados;

    private RecyclerView PostLista;

    private DatabaseReference pDatabase;

    private static DatabaseReference pDatabaseUsuarios;

    private FirebaseAuth pAutenticacion;
    private FirebaseAuth.AuthStateListener pAutenticacionOyente;

    AutoCompleteTextView pEstados;
    String item[]={
            "Amazonas", "Anzoátegui", "Apure", "Aragua",
            "Barinas", "Bolívar", "Carabobo", "Cojedes",
            "Delta Amacuro", "Distrito Capital", "Falcón", "Guárico",
            "Lara", "Mérida", "Miranda", "Monagas",
            "Nueva Esparta", "Portuguesa", "Sucre", "Táchira",
            "Trujillo", "Vargas", "Yaracuy", "Zulia"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Auto completador de estados en el menu principal
        pEstados = (AutoCompleteTextView) findViewById(R.id.autoCompleteEstados);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getApplication(), android.R.layout.simple_expandable_list_item_1, item);
        pEstados.setAdapter(adapter);

        BuscarEstados = (ImageButton) findViewById(R.id.BuscarEstado);

        // Obtencion de la imagen en el Almacenamiento interno del telefono
        BuscarEstados.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String val_estado = pEstados.getText().toString().trim();
                if(!TextUtils.isEmpty(val_estado)){
                    // Ejecuto la accion
                    Intent VistaEstado = new Intent(MainActivity.this, onepost.class);
                    VistaEstado.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    VistaEstado.putExtra("Valor", val_estado);
                    startActivity(VistaEstado);
                }
            }
        });


        pAutenticacion = FirebaseAuth.getInstance();
        pAutenticacionOyente = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {

                    Intent loginIntent = new Intent(MainActivity.this, Logueo.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
            }
        };


        //Me ubico en el contenidod e los posts los cuales se encuentran en la llave
        //'Principal' de la base de datos
        pDatabase = FirebaseDatabase.getInstance().getReference().child("Principal");
        pDatabaseUsuarios = FirebaseDatabase.getInstance().getReference().child("Usuarios");

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


        //Barra de Tareas
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Boton de Accion Flotante
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent(MainActivity.this, Publicador.class));
            }
        });

        //Apertura y Cierre del Dibujante
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Vista del Dibujante
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ChequeoExistencia_de_Usuario();

        //Color del menu lateral
        navigationView.setItemIconTintList(null);
    }

    /// CREA EL MENU CON EL NOMBRE DE USUARIO Y SU IMAGEN RECORTADA
    private void Usuario_Presente_Menu() {

        if (pAutenticacion.getCurrentUser() != null) {

            final String ID_USUARIO = pAutenticacion.getCurrentUser().getUid();
            final TextView ID_U = (TextView) findViewById(R.id.Name_Current);

            pDatabaseUsuarios.child(ID_USUARIO).child("Seudonimo").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String Nombre = (String) dataSnapshot.getValue();
                    ID_U.setText(Nombre);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            final CircularImageView circularImageView = (CircularImageView) findViewById(R.id.Profile_Current);

            pDatabaseUsuarios.child(ID_USUARIO).child("Imagen").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String Imagen = (String) dataSnapshot.getValue();

                    Picasso.with(getApplicationContext()).load(Imagen).networkPolicy(NetworkPolicy.OFFLINE).into(circularImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext()).load(Imagen).into(circularImageView);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        pAutenticacion.addAuthStateListener(pAutenticacionOyente);

        FirebaseRecyclerAdapter<Principal,PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Principal, PostViewHolder>(
                Principal.class,
                R.layout.filas_post,
                PostViewHolder.class,
                pDatabase
        ){
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, Principal model, int position) {

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

                        Intent ConfigIntent = new Intent(MainActivity.this, Configuracion.class);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Error aca el 30/05/17 estoy asignando el perfil del menu pero estando en el login explota por no auntenticar primero
        Usuario_Presente_Menu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // El elemento de la barra de acción de la manija hace clic aquí. La barra de acción
        // gestiona automáticamente los clics en el botón Inicio / Arriba, tanto tiempo
        // como se especifica una actividad principal en AndroidManifest.xml.

        int id = item.getItemId();

        //Configuracion
        if (id == R.id.Configuracion) {
            Intent ConfigIntent = new Intent(MainActivity.this, Configuracion.class);
            ConfigIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(ConfigIntent);
        }

        //Cerrar Sesion
        if (id == R.id.Salir_Usuario) {
            Cerrar_Sesion();
        }
        return super.onOptionsItemSelected(item);
    }

    private void Cerrar_Sesion() {
        pAutenticacion.signOut();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_expreso) {

            // Accion de los Posts Unicos
            Intent VistaEstado = new Intent(MainActivity.this, onepost.class);
            VistaEstado.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            VistaEstado.putExtra("Valor","VSE");
            startActivity(VistaEstado);

        } else if (id == R.id.nav_config) {
            //Configuracion de la cuenta
            Intent ConfigIntent = new Intent(MainActivity.this, Configuracion.class);
            ConfigIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(ConfigIntent);

        } else if (id == R.id.nav_cerrar) {
            Cerrar_Sesion();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
