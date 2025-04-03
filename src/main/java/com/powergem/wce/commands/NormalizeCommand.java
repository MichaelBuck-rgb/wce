package com.powergem.wce.commands;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powergem.worstcasetrlim.model.*;
import picocli.CommandLine;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "normalized",
        description = "Converts the specified WClusterTrLimSumJson.json into a 'normalized' json file.",
        usageHelpWidth = 132
)
public final class NormalizeCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "The WClusterTrLimSumJson.json to import")
  private Path jsonFile;

  @Override
  public Integer call() throws Exception {
    if (Files.notExists(this.jsonFile)) {
      System.err.println("File '" + this.jsonFile + "' does not exist.");
      return 1;
    }

    ObjectMapper mapper = new ObjectMapper();

    ObjectWriter objectWriter = mapper.writer(new DefaultPrettyPrinter());
    JsonGenerator generator = objectWriter.createGenerator(System.out);

    final List<String> busSchema = getSchema(Bus.class);

    final Function<Bus, List<Object>> busRecordMaker = new RecordMaker<>(Bus.class);

    Consumer<Bus> busConsumer = new ValueConsumer<>(busRecordMaker, generator);

    final List<String> stressGenSchema = getSchema(StressGen.class);

    final Function<StressGen, List<Object>> stressGenRecordMaker = new RecordMaker<>(StressGen.class);

    Consumer<StressGen> stressGenConsumer = new ValueConsumer<>(stressGenRecordMaker, generator);

    final List<String> flowgateSchema = getSchema(Flowgate.class);

    final Function<Flowgate, List<Object>> flowgateRecordMaker = new RecordMaker<>(Flowgate.class);

    Consumer<Flowgate> flowgateConsumer = new ValueConsumer<>(flowgateRecordMaker, generator);

    final List<String> branchTerminalSchema = getSchema(BranchTerminal.class);

    final Function<BranchTerminal, List<Object>> branchTerminalRecordMaker = new RecordMaker<>(BranchTerminal.class);

    Consumer<BranchTerminal> branchTerminalConsumer = new ValueConsumer<>(branchTerminalRecordMaker, generator);

    generator.writeStartObject();

    generator.writeArrayFieldStart("scenarios");

    try (
            JsonParser jsonParser = mapper.createParser(this.jsonFile.toFile())) {
      JsonToken jsonToken = jsonParser.nextToken();
      if (jsonToken == JsonToken.START_OBJECT) {
        jsonToken = jsonParser.nextToken();
        if (jsonToken == JsonToken.FIELD_NAME) {
          if ("wcResults".equals(jsonParser.currentName())) {

            generator.writeStartObject();

            jsonToken = jsonParser.nextToken();
            if (jsonToken == JsonToken.START_ARRAY) {
              // individual wcResult
              jsonToken = jsonParser.nextToken();
              if (jsonToken == JsonToken.START_OBJECT) {
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                  switch (jsonParser.currentName()) {
                    case "id":
                      jsonToken = jsonParser.nextToken();
                      if (jsonToken == JsonToken.VALUE_STRING) {
                        generator.writeStringField("id", jsonParser.getText());
                      } else {
                        throw new UnsupportedOperationException(jsonParser.currentName());
                      }
                      break;

                    case "title":
                      jsonToken = jsonParser.nextToken();
                      if (jsonToken == JsonToken.VALUE_STRING) {
                        generator.writeStringField("title", jsonParser.getText());
                      } else {
                        throw new UnsupportedOperationException(jsonParser.currentName());
                      }
                      break;

                    case "version":
                      jsonToken = jsonParser.nextToken();
                      if (jsonToken == JsonToken.VALUE_STRING) {
                        generator.writeStringField("version", jsonParser.getText());
                      } else {
                        throw new UnsupportedOperationException(jsonParser.currentName());
                      }
                      break;

                    case "buses":
                      writeTable("buses", busSchema, busConsumer, Bus.class, generator, jsonParser);
                      break;

                    case "StressGens":
                      writeTable("stressGens", stressGenSchema, stressGenConsumer, StressGen.class, generator, jsonParser);
                      break;

                    case "flowgates":
                      writeTable("flowgates", flowgateSchema, flowgateConsumer, Flowgate.class, generator, jsonParser);
                      break;

                    case "branchTerminalList":
                      writeTable("branchTerminalList", branchTerminalSchema, branchTerminalConsumer, BranchTerminal.class, generator, jsonParser);
                      break;

                    default:
                      throw new UnsupportedOperationException(jsonParser.currentName());
                  }
                }
              } else {
                throw new UnsupportedOperationException(jsonToken.toString());
              }
            } else {
              throw new UnsupportedOperationException(jsonToken.toString());
            }

            generator.writeEndObject();
          } else {
            throw new UnsupportedOperationException(jsonParser.currentName());
          }
        } else {
          throw new UnsupportedOperationException(jsonToken.toString());
        }
      } else {
        throw new UnsupportedOperationException(jsonToken.toString());
      }
    }

    generator.writeEndArray();

    generator.writeEndObject();

    generator.close();

    return 0;
  }

  private <T extends Record> void writeTable(String fieldName, List<String> busSchema, Consumer<T> busConsumer, Class<T> recordClass, JsonGenerator generator, JsonParser jsonParser) throws IOException {
    generator.writeObjectFieldStart(fieldName);

    generator.writeArrayFieldStart("schema");
    for (String columnName : busSchema) {
      generator.writeString(columnName);
    }
    generator.writeEndArray();

    generator.writeArrayFieldStart("table");
    parseObjects(jsonParser, recordClass, busConsumer);
    generator.writeEndArray();
    generator.writeEndObject();

  }

  private <T> void parseObjects(JsonParser jsonParser, Class<T> objectClass, Consumer<T> objectConsumer) throws
          IOException {
    JsonToken jsonToken = jsonParser.nextToken();
    if (jsonToken == JsonToken.START_ARRAY) {
      while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
        T object = parseObject(jsonParser, objectClass);
        objectConsumer.accept(object);
      }
    } else {
      throw new UnsupportedOperationException(jsonParser.currentName());
    }
  }

  private <T> T parseObject(JsonParser jsonParser, Class<T> objectClass) throws IOException {
    JsonToken jsonToken = jsonParser.currentToken();
    if (jsonToken == JsonToken.START_OBJECT) {
      return jsonParser.readValueAs(objectClass);
    } else {
      throw new UnsupportedOperationException(jsonParser.currentName());
    }
  }

  private static final class RecordMaker<T extends Record> implements Function<T, List<Object>> {
    private final Map<String, Method> accessors;

    public RecordMaker(Class<T> recordClass) {
      RecordComponent[] recordComponents = recordClass.getRecordComponents();
      BinaryOperator<Method> mergeFunction = (method, method2) -> method;
      this.accessors = Arrays.stream(recordComponents).collect(Collectors.toMap(RecordComponent::getName, RecordComponent::getAccessor, mergeFunction, LinkedHashMap::new));
    }

    @Override
    public List<Object> apply(T record) {
      return accessors.values()
              .stream().map(method -> {
                try {
                  return method.invoke(record);
                } catch (IllegalAccessException | InvocationTargetException e) {
                  throw new RuntimeException(e);
                }
              })
              .toList();
    }
  }

  private <T extends Record> List<String> getSchema(Class<T> recordClass) {
    RecordComponent[] recordComponents = recordClass.getRecordComponents();
    return Arrays.stream(recordComponents)
            .map(RecordComponent::getName)
            .toList();
  }

  private static final class ValueConsumer<T extends Record> implements Consumer<T> {
    private final Function<T, List<Object>> recordMaker;
    private final JsonGenerator jsonGenerator;

    public ValueConsumer(Function<T, List<Object>> recordMaker, JsonGenerator jsonGenerator) {
      this.recordMaker = recordMaker;
      this.jsonGenerator = jsonGenerator;
    }

    @Override
    public void accept(T record) {
      List<Object> values = this.recordMaker.apply(record);
      try {
        jsonGenerator.writeStartArray();
        for (Object value : values) {
          if (value == null) {
          } else if (value.getClass() == Integer.class) {
            jsonGenerator.writeNumber((Integer) value);
          } else if (value.getClass() == Double.class) {
            jsonGenerator.writeNumber((Double) value);
          } else if (value.getClass() == String.class) {
            jsonGenerator.writeString(String.valueOf(value));
          } else if (value instanceof Collection) {
          } else if (value.getClass().isArray()) {
          } else {
            throw new UnsupportedOperationException(value.getClass().getName());
          }
        }
        jsonGenerator.writeEndArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
