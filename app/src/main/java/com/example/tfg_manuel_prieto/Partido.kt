package com.example.tfg_manuel_prieto

data class Partido(
    val id: String = "",
    val equipo1: String = "",
    val equipo2: String = "",
    val marcador1: Int = 0,
    val marcador2: Int = 0,
    val fase: String = "",
    val idTorneo: String = ""
)