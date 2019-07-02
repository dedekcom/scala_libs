import re
from typing import List


DbLoadModeSeparator: int = 0
DbLoadModeCode: int      = 1

class Db:

    def __init__(self, filename: str = None, loadMode: int = DbLoadModeSeparator, separator: str = '|'):
        self.separator: str = separator
        self.arDb: List[List[str]] = []
        if filename is not None:
            self.load(filename, loadMode, separator)

    def freeDb(self):
        self.arDb: List[List[str]] = []

    def loadDb(self, filename: str, separator: str = '|'):
        self.load(filename, DbLoadModeSeparator, separator)

    def loadCode(self, filename: str):
        self.load(filename, DbLoadModeCode, ' ')

    def load(self, filename: str, loadMode: int, separator: str):
        self.freeDb()
        self.separator = separator
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

    def findInRows(self, func) -> List[str]:
        for row in self.arDb:
            if func(row):
                return row
        return None

    def findRowId(self, row: List[str]) -> int:
        for r in range(len(self.arDb)):
            if self.arDb[r] == row:
                return r
        return -1

    def containsRow(self, row: List[str]) -> bool:
        return self.findRowId(row) != -1

    def fold(self, func, initial):
        for row in self.arDb:
            initial = func(initial, row)
        return initial

    def addRow(self, row: List[str]) -> 'Db':
        self.arDb.append(row.copy())
        return self

    def addToRow(self, idRow: int, element: str) -> 'Db':
        self.getRow(idRow).append(element)
        return self

    def addToLastRow(self, element: str) -> 'Db':
        return self.addToRow(-1, element)

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

    def sortDb(self) -> 'Db':
        self.arDb = sorted(self.arDb)
        return self

    ##############################  set operations that return new Db ##################

    def copyDb(self) -> 'Db':
        return Db().addToDb(self)

    def mapDb(self, func) -> 'Db':
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

    def distinctDb(self) -> 'Db':
        return self.copyDb().sortDb().distinctNeighbours()

    def subCommon(self, secondDb: 'Db') -> 'Db':
        def internal(accDb: 'Db', nextRow: List[str]):
            if secondDb.containsRow(nextRow):
                return accDb
            else:
                return accDb.addRow(nextRow)
        return self.fold(internal, Db())

    def xorDb(self, secondDb: 'Db') -> 'Db':
        db1 = self.subCommon(secondDb)
        db2 = secondDb.subCommon(self)
        return db1.addToDb(db2)
