package com.example.tfg_manuel_prieto

data class Reporte(
    val reporteId: String = "",
    val messageId: String = "",
    val userId: String = "",
    val motivo: String = "",
    val resuelto: Boolean = false,
    val mensaje: String = "",
)