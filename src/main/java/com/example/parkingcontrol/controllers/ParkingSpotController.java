package com.example.parkingcontrol.controllers;

import com.example.parkingcontrol.dtos.ParkingSpotDto;
import com.example.parkingcontrol.models.ParkingSpotModel;
import com.example.parkingcontrol.services.ParkingSpotService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto){

        if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())){

            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Plate already registred!");

        }

        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())){

            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking spot already in use!");

        }

        if(parkingSpotService.existsByApartamentAndBlock(parkingSpotDto.getApartament(), parkingSpotDto.getBlock())){

            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking spot already registred to that apartament");

        }

        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel); //converte dto em model
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC"))); //colocando a data atual

        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel)); //salvando

    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 8, size = 10, sort = "id", direction = Sort.Direction.ASC)Pageable pageable){

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));

    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if(!parkingSpotModelOptional.isPresent()){

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found.");

        }

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleterParkingSpot(@PathVariable(value = "id")UUID id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if(!parkingSpotModelOptional.isPresent()){

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found.");

        }

        parkingSpotService.delete(parkingSpotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking Spot Deleted.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto){

        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()){

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found.");

        }

        var parkingSpotModel = new ParkingSpotModel();

        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));

    }

}
