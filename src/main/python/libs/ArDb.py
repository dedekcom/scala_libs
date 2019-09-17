import re
from typing import List, Dict, Callable, TypeVar


T = TypeVar('T')

DbLoadModeSeparator: int = 0
DbLoadModeCode: int      = 1

class Db:

    def __init__(self, filename: str = None, loadMode: int = DbLoadModeSeparator, separator: str = '|'):
        self.separator: str = separator
        self.arDb: List[List[str]] = []
        if filename is not None:
            self.load(filename, loadMode, separator)

    def freeDb(self) -> 'Db':
        self.arDb: List[List[str]] = []
        return self

    def setSeparator(self, separator: str):
        self.separator = separator

    def loadDb(self, filename: str, separator: str = '|') -> 'Db':
        return self.load(filename, DbLoadModeSeparator, separator)

    def loadCode(self, filename: str) -> 'Db':
        return self.load(filename, DbLoadModeCode, ' ')

    def load(self, filename: str, loadMode: int, separator: str) -> 'Db':
        self.freeDb()
        self.setSeparator(separator)
        try:
            dbfile = open(filename)
            for line in dbfile.readlines():
                if loadMode == DbLoadModeSeparator:
                    self.arDb.append(line.rstrip().split(separator))
                elif loadMode == DbLoadModeCode:
                    self.arDb.append(re.split("\s+", line.strip()))
            dbfile.close()
        except Exception as e:
            print("Db: unable to load " + filename + ": " + str(e))
        return self

    def save(self, filename):
        try:
            dbfile = open(filename, "w+")
            lines = map(lambda line: self.separator.join(line), self.arDb)
            dbfile.write("\n".join(lines))
            dbfile.close()
        except Exception as e:
            print("Db: unable to save " + filename + ": " + str(e))

    def printDb(self):
        lines = map(lambda line: self.separator.join(line) + " :" + str(len(line)), self.arDb)
        print("\n".join(lines))

    def getRows(self) -> List[List[str]]:
        return self.arDb

    def getRow(self, idRow: int) -> List[str]:
        return self.arDb[idRow]

    def dbLen(self) -> int:
        return len(self.arDb)

    def rowLen(self, idRow: int) -> int:
        return len(self.arDb[idRow])

    def getCell(self, row: int, col: int) -> str:
        return (self.arDb[row])[col]

    def isCell(self, idRow: int, idCol: int) -> bool:
        return True if idRow >= 0 and idRow < self.dbLen() and idCol >= 0 and idCol < self.rowLen(idCol) else False

    def findByRow(self, element: str, idCol: int = 0) -> str:
        if idCol < 0:
            return None
        res = None
        idrow = 0
        size = self.dbLen()
        while res is None and idrow < size:
            if idCol < self.rowLen(idrow) and self.getCell(idrow, idCol) == element:
                res = self.getRow(idrow)
            idrow += 1
        return res

    def findInRows(self, func: Callable[[List[str]], bool]) -> List[str]:
        for row in self.arDb:
            if func(row):
                return row
        return None

    def findAllRows(self, func: Callable[[List[str]], bool]) -> 'Db':
        result = []
        for row in self.arDb:
            if func(row):
                result.append(row)
        return Db().createDb(result)

    def findRowId(self, row: List[str]) -> int:
        for r in range(len(self.arDb)):
            if self.arDb[r] == row:
                return r
        return -1

    def containsRow(self, row: List[str]) -> bool:
        return self.findRowId(row) != -1

    def fold(self, func: Callable[[T, List[str]], bool], initial: T) -> T:
        for row in self.arDb:
            initial = func(initial, row)
        return initial

    def addRow(self, row: List[str]) -> 'Db':
        self.arDb.append(row.copy())
        return self

    def addRowRef(self, row: List[str]) -> 'Db':
        self.arDb.append(row)
        return self

    def addToRow(self, idRow: int, element: str) -> 'Db':
        self.getRow(idRow).append(element)
        return self

    def extendRow(self, idRow: int, elements: List[str]) -> 'Db':
        self.getRow(idRow).extend(elements)
        return self

    def extendLastRow(self, elements: List[str]) -> 'Db':
        if self.isEmpty():
            return self.addRow(elements)
        else:
            return self.extendRow(-1, elements)

    def addToLastRow(self, element: str) -> 'Db':
        return self.extendLastRow([element])

    def removeRowAt(self, idRow: int) -> 'Db':
        if idRow >=0 and idRow < self.dbLen():
            self.arDb.pop(idRow)
        return self

    def removeRow(self, row: List[str]) -> 'Db':
        return self.removeRowAt(self.findRowId(row))

    def isEmpty(self) -> bool:
        return self.dbLen() == 0

    def lastRow(self) -> List[str]:
        return self.arDb[-1]

    def firstRow(self) -> List[str]:
        return self.arDb[0]

    def addToDb(self, secondDb: 'Db') -> 'Db':
        for row in secondDb.getRows():
            self.addRow(row)
        return self

    def createDb(self, listDb: List[List[str]]) -> 'Db':
        self.freeDb()
        for row in listDb:
            self.addRow(row)
        return self

    def sortDb(self) -> 'Db':
        self.arDb = sorted(self.arDb)
        return self

    def equals(self, db: 'Db') -> bool:
        return self.arDb == db.arDb

    ##############################  set operations that return new Db ##################

    def copyDb(self) -> 'Db':
        return Db().addToDb(self)

    def reversedDb(self) -> 'Db':
        result = self.copyDb()
        result.arDb.reverse()
        return result

    def mapDb(self, func: Callable[[List[str]], List[str]]) -> 'Db':
        db = Db()
        for row in self.arDb:
            db.addRow(func(row))
        return db

    def distinctNeighbours(self) -> 'Db':
        def internal(accDb: 'Db', nextRow: List[str]) -> 'Db':
            if accDb.isEmpty() or accDb.lastRow() != nextRow:
                return accDb.addRow(nextRow)
            else:
                return accDb
        return self.fold(internal, Db())

    def rowToKey(self, row: List[str]) -> str:
        return self.separator.join(row)

    def distinctDb(self) -> 'Db':
        db = Db()
        dic: Dict[str, int] = {}
        for row in self.arDb:
            key = self.rowToKey(row)
            if dic.get(key, None) is None:
                db.addRow(row)
                dic[key] = 1
        return db

    def subCommon(self, secondDb: 'Db') -> 'Db':
        secDict = secondDb.toDictStd()
        def internal(accDb: 'Db', nextRow: List[str]):
            if secDict.get(self.rowToKey(nextRow), None) is not None:
                return accDb
            else:
                return accDb.addRow(nextRow)
        return self.fold(internal, Db())

    def xorDb(self, secondDb: 'Db') -> 'Db':
        db1 = self.subCommon(secondDb)
        db2 = secondDb.subCommon(self)
        return db1.addToDb(db2)

    def toDict(self, funcOnRow: Callable[[List[str]], str]) -> Dict[str, List[str]]:
        res: Dict[str, List[str]] = {}
        for row in self.getRows():
            res[funcOnRow(row)] = row
        return res

    def toDictStd(self) -> Dict[str, List[str]]:
        return self.toDict(lambda row: self.rowToKey(row))
