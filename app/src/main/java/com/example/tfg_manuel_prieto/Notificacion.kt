package com.example.tfg_manuel_prieto
data class Notificacion(
    var titulo: String = "",
    var cuerpo: String = "",
    var leido: Boolean = false,
    var chatId: String = "",
    var nombreTorneo: String = "",
    var nombreUsuario: String = "",
    var userId: String = ""
)