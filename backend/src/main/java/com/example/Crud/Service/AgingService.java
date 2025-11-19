package com.example.Crud.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AgingService {

    @Autowired
    private TurnoService turnoService;

    @Scheduled(fixedRate = 60000)
    public void actualizarPrioridadesAging() {
        turnoService.actualizarAgingTurnos();
    }
}
