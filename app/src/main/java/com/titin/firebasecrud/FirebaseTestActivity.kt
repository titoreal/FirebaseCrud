package com.titin.firebasecrud

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FirebaseTestActivity : AppCompatActivity() {
    // Referencias a Firebase
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_test)
        
        findViewById<Button>(R.id.btnTestDatabase).setOnClickListener {
            testRealtimeDatabase()
        }
        
        findViewById<Button>(R.id.btnTestStorage).setOnClickListener {
            testFirebaseStorage()
        }
    }
    
    private fun testRealtimeDatabase() {
        // Crear un nodo de prueba
        val testRef = database.getReference("test_connection")
        val timestamp = System.currentTimeMillis()
        
        testRef.setValue(timestamp).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "¡Database conectada! Timestamp guardado: $timestamp", 
                    Toast.LENGTH_LONG).show()
                
                // Verificar leyendo el valor que acabamos de escribir
                testRef.get().addOnSuccessListener { snapshot ->
                    val readValue = snapshot.getValue(Long::class.java)
                    Toast.makeText(this, "Valor leído: $readValue", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error en Database: ${task.exception?.message}", 
                    Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun testFirebaseStorage() {
        // Crear un archivo de texto simple para subir
        val testContent = "Test desde la app ${System.currentTimeMillis()}"
        val stream = testContent.byteInputStream()
        
        // Subir al Storage
        val testFileRef = storage.reference.child("test_connection/${UUID.randomUUID()}.txt")
        testFileRef.putStream(stream).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "¡Storage conectado! Archivo subido correctamente", 
                    Toast.LENGTH_LONG).show()
                
                // Verificar obteniendo la URL del archivo
                testFileRef.downloadUrl.addOnSuccessListener { uri ->
                    Toast.makeText(this, "URL del archivo: $uri", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error en Storage: ${task.exception?.message}", 
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}