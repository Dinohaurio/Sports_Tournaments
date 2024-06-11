package com.example.tfg_manuel_prieto

data class Equipo(
    val id: String = "",
    val nombre: String = "",
    val capitan: String = "",
    val idCapitan: String = "",
    val numeroParticipantes: Int = 0,
    val nombreTorneo: String = "",
    var idTorneo: String = "" // Nuevo campo para almacenar el ID del torneo
)