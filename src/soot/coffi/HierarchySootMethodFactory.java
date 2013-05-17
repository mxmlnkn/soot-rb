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

import java.util.List;
import java.util.ArrayList;
import soot.rbclassload.HierarchySootMethod;
import soot.rbclassload.HierarchyInstruction;
import soot.rbclassload.Operand;

public class HierarchySootMethodFactory {

  private cp_info[] m_constantPool;
  private attribute_info[] m_attributes;
  private ConstantPoolReader m_constantReader;

  public HierarchySootMethodFactory(){
    m_constantReader = new ConstantPoolReader();
  }

  public HierarchySootMethod create(ClassFile classFile, int index){
    method_info methodInfo = classFile.methods[index];
    cp_info[] constantPool = classFile.constant_pool;
    attribute_info[] attributes = classFile.attributes;

    m_constantPool = constantPool;
    m_attributes = attributes;

    CONSTANT_Utf8_info utf8_name = (CONSTANT_Utf8_info) m_constantPool[methodInfo.name_index];
    String name = utf8_name.convert();

    String type_descr = cp_info.getTypeDescr(m_constantPool, methodInfo.descriptor_index);
    String[] tokens = type_descr.split("\\)");
    String return_desc = tokens[1];
    String parameter_desc = tokens[0].substring(1);

    String returnType = ClassFile.parseDesc(return_desc, ",");
    String parsedParameterTypes = ClassFile.parseDesc(parameter_desc, ",");
    String[] parametersArray = parsedParameterTypes.split(",");
    List<String> parameterTypes = new ArrayList<String>();
    for(String param : parametersArray){
      String trimmed = param.trim();
      if(trimmed.equals("")){
        continue;
      }
      parameterTypes.add(param);
    }

    attribute_info exceptions_attr = findExceptionsAttribute(methodInfo);
    List<String> exceptionTypes = new ArrayList<String>();
    if(exceptions_attr != null){
      Exception_attribute ex_attribute = (Exception_attribute) exceptions_attr;
      for(int table_index : ex_attribute.exception_index_table){
        exceptionTypes.add(m_constantReader.get(table_index, m_constantPool));
      }      
    }

    List<String> code_attr_exceptions = getCodeAttrExceptions(methodInfo, 
      constantPool);

    Code_attribute code_attribute = methodInfo.locate_code_attribute();
    HierarchySootMethod ret = new HierarchySootMethod(name, returnType,
      parameterTypes, exceptionTypes, methodInfo.access_flags, classFile,
      methodInfo, code_attr_exceptions);

    return ret;
  }

  private List<String> getCodeAttrExceptions(method_info methodInfo, 
    cp_info[] constant_pool){

    List<String> ret = new ArrayList<String>();
    Code_attribute code_attr = methodInfo.locate_code_attribute(); 
    if(code_attr == null){
      return ret;
    }

    exception_table_entry[] ex_table = code_attr.exception_table;
    for(int i = 0; i < ex_table.length; ++i){
      int catch_type = ex_table[i].catch_type;
      if(catch_type != 0){
        ret.add(m_constantReader.get(catch_type, constant_pool));
      } else {
        ret.add("java.lang.Throwable");
      }
    }
    return ret;
  }

  public List<HierarchyInstruction> parseInstructions(ClassFile classFile, method_info methodInfo){
    m_constantPool = classFile.constant_pool;
    List<HierarchyInstruction> instructions = new ArrayList<HierarchyInstruction>();
    Instruction inst = classFile.parseMethod(methodInfo);
    while(inst != null){
      HierarchyInstruction hinst = parseInstruction(inst);      
      instructions.add(hinst);
      inst = inst.next;
    }
    return instructions;
  }

  private HierarchyInstruction parseInstruction(Instruction inst){
    String name = inst.name;
    List<Operand> operands = new ArrayList<Operand>();
    if(inst instanceof Instruction_noargs){
      //ignore
    } else if(inst instanceof Instruction_byte){
      Instruction_byte inst_byte = (Instruction_byte) inst;
      String arg0 = Byte.valueOf(inst_byte.arg_b).toString();
      operands.add(new Operand(arg0, "byte"));
    } else if(inst instanceof Instruction_bytevar){
      Instruction_bytevar inst_bytevar = (Instruction_bytevar) inst;
      String arg0 = Integer.valueOf(inst_bytevar.arg_b).toString();
      operands.add(new Operand(arg0, "byte"));
    } else if(inst instanceof Instruction_byteindex){
      Instruction_byteindex inst_byteindex = (Instruction_byteindex) inst;  
      String arg0 = m_constantReader.get(inst_byteindex.arg_b, m_constantPool);
      String type0 = m_constantReader.getType(inst_byteindex.arg_b, m_constantPool);
      operands.add(new Operand(arg0, type0));
    } else if(inst instanceof Instruction_int){
      Instruction_int inst_int = (Instruction_int) inst;
      String arg0 = Integer.valueOf(inst_int.arg_i).toString();
      operands.add(new Operand(arg0, "int"));
    } else if(inst instanceof Instruction_intvar){
      Instruction_intvar inst_intvar = (Instruction_intvar) inst;
      String arg0 = Integer.valueOf(inst_intvar.arg_i).toString();
      operands.add(new Operand(arg0, "int"));
    } else if(inst instanceof Instruction_intindex){
      Instruction_intindex inst_intindex = (Instruction_intindex) inst;
      String arg0 = m_constantReader.get(inst_intindex.arg_i, m_constantPool);
      String type0 = m_constantReader.getType(inst_intindex.arg_i, m_constantPool);
      operands.add(new Operand(arg0, type0));
    } else if(inst instanceof Instruction_intbranch){
      Instruction_intbranch inst_intbranch = (Instruction_intbranch) inst;
      String arg0 = Integer.valueOf(inst_intbranch.arg_i).toString();
      operands.add(new Operand(arg0, "int"));
    } else if(inst instanceof Instruction_longbranch){
      Instruction_longbranch inst_longbranch = (Instruction_longbranch) inst;
      String arg0 = Integer.valueOf(inst_longbranch.arg_i).toString();
      operands.add(new Operand(arg0, "long"));
    } else if(inst instanceof Instruction_Lookupswitch){
      //ignore
    } else if(inst instanceof Instruction_Tableswitch){
      //ignore
    } else if(inst instanceof Instruction_Newarray){
      Instruction_Newarray inst_newarray = (Instruction_Newarray) inst;
      String str = inst_newarray.toString(m_constantPool);
      String[] tokens = str.split(" ");
      name = tokens[0];
      operands.add(new Operand(tokens[1], "class_ref"));
    } else {
      throw new RuntimeException("unknown instruction type: "+inst.toString());
    } 
    HierarchyInstruction ret = new HierarchyInstruction(name, operands);
    return ret;
  }
  
  private attribute_info findExceptionsAttribute(method_info mi){
    if(m_attributes== null){
      return null;
    }
    for(attribute_info attribute : m_attributes){
      int name_index = attribute.attribute_name;
      CONSTANT_Utf8_info utf8_name = (CONSTANT_Utf8_info) m_constantPool[name_index];
      String attribute_name = utf8_name.convert();
      if(attribute_name.equals(attribute_info.Exceptions)){
        return attribute;
      }
    }
    return null;
  }
}
