# print数据结构说明

## [PrintDataHandlerDto.java](dto%2FPrintDataHandlerDto.java)

该dto适用于[AbstratePrintDataHandler.java](..%2Fservice%2Fstrategy%2FAbstratePrintDataHandler.java)
的入参，防止后续需要拓展,初始即定义完善的数据结构

[PrintFileConfigTypeDto.java](dto%2FPrintFileConfigTypeDto.java)
对应的[FileConfigTypeRStrategy.java](..%2Fservice%2Fstrategy%2Ffileconfig%2FFileConfigTypeRStrategy.java)
防止该策略后续需要更多入参，采用继承[PrintDataHandlerDto.java](dto%2FPrintDataHandlerDto.java)
的方式，保证基础参数不会收到影响

[PrintImageTypeDto.java](dto%2FPrintImageTypeDto.java)