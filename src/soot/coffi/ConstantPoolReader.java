/* Soot - a J*va Optimization Framework
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/* The soot.rbclassload package is:
 * Copyright (C) 2012 Phil Pratt-Szeliga
 * Copyright (C) 2012 Marc-Andre Laverdiere-Papineau
 */

package soot.coffi;

public class ConstantPoolReader {

  public String convertClass(String class_name){
    if(class_name.startsWith("[")){
      return parseDesc(class_name);
    } else {
      return class_name.replace('/','.');
    }
  }

  public String get(int index, cp_info[] constant_pool){
    cp_info entry = constant_pool[index];
    if(entry instanceof CONSTANT_Class_info){
      CONSTANT_Class_info class_info = (CONSTANT_Class_info) entry;
      String class_name = get(class_info.name_index, constant_pool);
      return convertClass(class_name);
    } else if(entry instanceof CONSTANT_Methodref_info){
      CONSTANT_Methodref_info methodref_info = (CONSTANT_Methodref_info) entry;
      String class_name = get(methodref_info.class_index, constant_pool);
      class_name = convertClass(class_name);
      String subsig = getNameAndTypeInfo(methodref_info.name_and_type_index, 
        constant_pool, true);
      return "<"+class_name+": "+subsig+">";
    } else if(entry instanceof CONSTANT_Utf8_info){
      CONSTANT_Utf8_info utf8_info = (CONSTANT_Utf8_info) entry;
      String str = utf8_info.convert();
      return str;
    } else if(entry instanceof CONSTANT_Fieldref_info){ 
      CONSTANT_Fieldref_info fieldref_info = (CONSTANT_Fieldref_info) entry;
      String class_name = get(fieldref_info.class_index, constant_pool);      
      class_name = convertClass(class_name);
      String type = getNameAndTypeInfo(fieldref_info.name_and_type_index, 
        constant_pool, false);
      return "<"+class_name+": "+type+">";
    } else if(entry instanceof CONSTANT_String_info){
      CONSTANT_String_info string_info = (CONSTANT_String_info) entry;
      return get(string_info.string_index, constant_pool);
    } else if(entry instanceof CONSTANT_InterfaceMethodref_info){
      CONSTANT_InterfaceMethodref_info methodref_info = (CONSTANT_InterfaceMethodref_info) entry;
      String class_name = get(methodref_info.class_index, constant_pool);      
      class_name = convertClass(class_name);
      String subsig = getNameAndTypeInfo(methodref_info.name_and_type_index, 
        constant_pool, true);
      return "<"+class_name+": "+subsig+">";
    } else if(entry instanceof CONSTANT_Integer_info){
      CONSTANT_Integer_info integer_info = (CONSTANT_Integer_info) entry;
      return Integer.toString((int) integer_info.bytes);
    } else if(entry instanceof CONSTANT_Long_info){
      CONSTANT_Long_info long_info = (CONSTANT_Long_info) entry;
      return Long.toString((int) long_info.convert());
    } else if(entry instanceof CONSTANT_Float_info){
      CONSTANT_Float_info float_info = (CONSTANT_Float_info) entry;
      return Float.toString(float_info.convert());
    } else if(entry instanceof CONSTANT_Double_info){
      CONSTANT_Double_info double_info = (CONSTANT_Double_info) entry;
      return Double.toString(double_info.convert());
    } else {
      throw new RuntimeException("unknown type: "+entry);
    }
  }

  public String getType(int index, cp_info[] constant_pool){
    cp_info entry = constant_pool[index];
    if(entry instanceof CONSTANT_Class_info){
      return "class_ref";
    } else if(entry instanceof CONSTANT_Methodref_info){
      return "method_ref";
    } else if(entry instanceof CONSTANT_Utf8_info){
      return "string";
    } else if(entry instanceof CONSTANT_Fieldref_info){ 
      return "field_ref";
    } else if(entry instanceof CONSTANT_String_info){
      return "string";
    } else if(entry instanceof CONSTANT_InterfaceMethodref_info){
      return "method_ref";
    } else if(entry instanceof CONSTANT_Integer_info){
      return "int";
    } else if(entry instanceof CONSTANT_Long_info){
      return "long";
    } else if(entry instanceof CONSTANT_Float_info){
      return "float";
    } else if(entry instanceof CONSTANT_Double_info){
      return "double";
    } else {
      throw new RuntimeException("unknown type: "+entry);
    }
  }

  private String getNameAndTypeInfo(int index, cp_info[] constant_pool, 
    boolean method){

    CONSTANT_NameAndType_info name_and_type_info = (CONSTANT_NameAndType_info) constant_pool[index];
    String name = get(name_and_type_info.name_index, constant_pool);
    String desc = get(name_and_type_info.descriptor_index, constant_pool);
  
    if(method){
      String return_type = ClassFile.parseMethodDesc_return(desc); 
      String params = ClassFile.parseMethodDesc_params(desc); 
      String ret = return_type+" "+name+"("+params+")";
      return ret;
    } else {
      return parseDesc(desc)+" "+name;
    }
  }

  private String parseDesc(String str){
    String ret = "";
    int array_count = 0;
    int index = 0;
    while(str.charAt(index) == '['){
      array_count++;
      ++index;
    }
    String param;
    if(str.charAt(index) == 'B'){
      param = "byte";
    } else if(str.charAt(index) == 'C'){
      param = "char";
    } else if(str.charAt(index) == 'D'){
      param = "double";
    } else if(str.charAt(index) == 'D'){
      param = "double";
    } else if(str.charAt(index) == 'F'){
      param = "float";
    } else if(str.charAt(index) == 'I'){
      param = "int";
    } else if(str.charAt(index) == 'J'){
      param = "long";
    } else if(str.charAt(index) == 'S'){
      param = "short";
    } else if(str.charAt(index) == 'Z'){
      param = "boolean";
    } else if(str.charAt(index) == 'V'){
      param = "void";
    } else if(str.charAt(index) == 'L'){
      ++index;
      param = "";
      while(str.charAt(index) != ';'){
        param += str.charAt(index);
        ++index;
      }
      param = param.replace('/','.');
    } else {
      str = str.replace('/','.');
      return str;
    }
    for(int i = 0; i < array_count; ++i){
      param += "[]";
    }
    return param;
  }
}
