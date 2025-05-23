package com.example.student.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.student.DTO.LaptopAssignmentDTO;
import com.example.student.DTO.LaptopDTO;
import com.example.student.DTO.LaptopListResponseDTO;
import com.example.student.DTO.LaptopbyIdDTO;
import com.example.student.DTO.LaptopdetailsDTO;
import com.example.student.DTO.PageSortDTO;
import com.example.student.DTO.SubjectMappingDTO;
import com.example.student.Service.LapTopService;
import com.example.student.entity.Gd_Laptop;
import com.example.student.repository.LapTopRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("laptop")
public class LapTopController {
	
	 @Autowired
	    private LapTopService laptopService;
	 
	 @Autowired 
	 private LapTopRepository laptoprepository;

	
	 @GetMapping("/")
	 public ResponseEntity<PageSortDTO<LaptopdetailsDTO>> getAllLaptopDetails(
	         @RequestParam(defaultValue = "0") int page,
	         @RequestParam(defaultValue = "10") int size,
	         @RequestParam(required = false) Boolean isAssigned,
	         @RequestParam(required = false) Boolean isActive)
	          {

	     Pageable pageable = PageRequest.of(page, size);
	     
	     // Pass the keyword and pagination details to the service
	     PageSortDTO<LaptopdetailsDTO> response = laptopService.getAllLaptopDetails(pageable, isAssigned, isActive);

	     return ResponseEntity.ok(response);
	 }
	 
	 @GetMapping("{laptopId}")
	    public LaptopbyIdDTO getLaptopDetails(@PathVariable Integer laptopId) {
	        return laptopService.getLaptopDetailsById(laptopId);
	    }



	    // POST a new laptop
	 @Operation(
			 summary="To add the All Laptop detail",
    		 description="To Add the All Detail"
			 )
	    @PostMapping("Add")
	    public ResponseEntity<?> saveLaptop(@RequestBody LaptopDTO dto) {
	        try {
	            LaptopDTO savedLaptop = laptopService.saveLaptop(dto);
	            return ResponseEntity.ok(savedLaptop);
	        } catch (DataIntegrityViolationException e) {
	            return ResponseEntity.badRequest().body("Laptop with this model number already exists.");
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("Failed to save laptop. Please check the data and try again.");
	        }
	    }
	 @Operation(
			 summary="To Assign Laptop to student",
			 description="To Assign Laptop to student"
			 ) 
	 @PostMapping("/assign")
	    public ResponseEntity<String> assignLaptopToStudent(@RequestBody LaptopAssignmentDTO request) {
	        String result = laptopService.assignLaptopToStudent(request.getLaptopId(), request.getStudentId());

	        if (result.contains("successfully")) {
	            return ResponseEntity.ok(result);
	        } else {
	            return ResponseEntity.badRequest().body(result);
	        }
	    }
	 
	 @PatchMapping("/update-status")
	 public ResponseEntity<String> updateLaptopStatus(@RequestBody Map<String, Object> requestBody) {
	     Integer laptopId = (Integer) requestBody.get("laptopId");
	     Boolean isAlive = (Boolean) requestBody.get("isAlive");

	     if (laptopId == null || isAlive == null) {
	         return ResponseEntity.badRequest().body("Laptop ID and isAlive status are required");
	     }

	     Optional<Gd_Laptop> optionalLaptop = laptoprepository.findById(laptopId);

	     if (!optionalLaptop.isPresent()) {
	         return ResponseEntity.badRequest().body("Laptop not found");
	     }

	     Gd_Laptop laptop = optionalLaptop.get();

	     if (!isAlive) {
	         // Trying to deactivate
	         if (laptop.getIS_ASSIGNED() == 1) {
	             return ResponseEntity.badRequest().body("Laptop is currently assigned and cannot be deactivated");
	         }
	         laptop.setIS_ALIVE(false);
	         laptoprepository.save(laptop);
	         return ResponseEntity.ok("Laptop deactivated successfully");
	     } else {
	         // Trying to activate
	         if (laptop.getIS_ASSIGNED() == 0) {
	             laptop.setIS_ALIVE(true);
	             laptoprepository.save(laptop);
	         }
	         return ResponseEntity.ok("Laptop activated successfully");
	     }
	 }


		 	   

	 }

