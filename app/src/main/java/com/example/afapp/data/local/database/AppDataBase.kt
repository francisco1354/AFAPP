package com.example.afapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.afapp.R // Importación de R necesaria para acceder a drawable
import com.example.afapp.data.local.user.UserDao
import com.example.afapp.data.local.user.UserEntity
import com.example.afapp.data.local.post.PostDao
import com.example.afapp.data.local.post.PostEntity
import com.example.afapp.data.local.comment.CommentDao
import com.example.afapp.data.local.comment.CommentEntity
import com.example.afapp.data.local.like.LikeDao
import com.example.afapp.data.local.like.LikeEntity
import com.example.afapp.domain.asfaltofashion.Category
import com.example.afapp.utils.ImageUtils
import com.example.afapp.utils.PasswordUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        CommentEntity::class,
        LikeEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun likeDao(): LikeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DB_NAME = "asfaltofashion.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    val userDao = database.userDao()
                                    val postDao = database.postDao()

                                    // ⬇️ CORREGIDO: Usando los nombres de recursos que existen en res/drawable
                                    val adminImage = ImageUtils.copyDrawableToInternalStorage(context, R.drawable.asfaltofashion, "admin_profile.jpg")
                                    val image1 = ImageUtils.copyDrawableToInternalStorage(context, R.drawable.altacostura, "altacostura.jpg")
                                    val image2 = ImageUtils.copyDrawableToInternalStorage(context, R.drawable.streetwear, "streetwear.jpg")
                                    val image3 = ImageUtils.copyDrawableToInternalStorage(context, R.drawable.grunge, "grunge.jpg")


                                    // --- USUARIOS DE EJEMPLO ---
                                    userDao.insert(UserEntity(
                                        id = 1,
                                        name = "Admin Asfalto",
                                        email = "admin@asfalto.cl",
                                        phone = "+56911112222",
                                        password = PasswordUtils.hashPassword("Admin123!"),
                                        profileImagePath = adminImage,
                                        isAdmin = true
                                    ))
                                    userDao.insert(UserEntity(
                                        id = 2,
                                        name = "Joan Doe",
                                        email = "joan@gmail.com",
                                        phone = "+5694545454535",
                                        password = PasswordUtils.hashPassword("Asfalto123!"),
                                        isAdmin = false
                                    ))

                                    // --- POSTS DE EJEMPLO ---
                                    if (postDao.count() == 0) {

                                        val contenido1 = "Tendencias de Alta Costura: El lujo se encuentra con la calle. Este año, las texturas ricas y los cortes dramáticos dominan el paisaje urbano."
                                        val contenido2 = "Guía Esencial de Streetwear: El layering es la clave. Cómo combinar piezas deportivas con básicos de moda para un look desenfadado pero intencional."
                                        val contenido3 = "Editorial Grunge: Explorando el estilo rebelde. Cuadros, cuero y cadenas para un look con actitud que redefine los límites de la moda urbana."

                                        val posts = listOf(
                                            PostEntity(id = UUID.randomUUID().toString(), title = "Alta Costura Urbana", summary = "Tendencias que vienen...", content = contenido1, category = Category.TENDENCIAS.name, authorId = 1, publishedAt = System.currentTimeMillis(), imageUrl = image1),
                                            PostEntity(id = UUID.randomUUID().toString(), title = "Streetwear: El Layering Perfecto", summary = "Domina el look urbano.", content = contenido2, category = Category.STREETWEAR.name, authorId = 1, publishedAt = System.currentTimeMillis() - 10000, imageUrl = image2),
                                            PostEntity(id = UUID.randomUUID().toString(), title = "Editorial: Estilo Grunge", summary = "La actitud regresa...", content = contenido3, category = Category.EDITORIAL.name, authorId = 1, publishedAt = System.currentTimeMillis() - 20000, imageUrl = image3)
                                        )
                                        posts.forEach { postDao.insert(it) }
                                    }
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}