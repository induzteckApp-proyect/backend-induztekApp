package com.induztek.usuarios.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class AvatarStorageService {
 private final Path directory;
 public AvatarStorageService(@Value("${app.upload-dir:uploads/avatars}") String path){
  directory=Paths.get(path).toAbsolutePath().normalize();
 }
 public String guardar(MultipartFile file) throws IOException {
  if(file==null||file.isEmpty()) throw new IllegalArgumentException("Selecciona una imagen.");
  if(file.getSize()>3*1024*1024) throw new IllegalArgumentException("La imagen no puede superar 3 MB.");
  String type=file.getContentType()==null?"":file.getContentType().toLowerCase();
  String ext=switch(type){case "image/jpeg"->".jpg";case "image/png"->".png";case "image/webp"->".webp";default->throw new IllegalArgumentException("Formato permitido: JPG, PNG o WEBP.");};
  Files.createDirectories(directory);
  String name=UUID.randomUUID()+ext;
  Path target=directory.resolve(name).normalize();
  if(!target.startsWith(directory)) throw new IllegalArgumentException("Nombre de archivo inválido.");
  try (var input = file.getInputStream()) {
   Files.copy(input,target,StandardCopyOption.REPLACE_EXISTING);
  }
  return "/media/avatars/"+name;
 }
}
