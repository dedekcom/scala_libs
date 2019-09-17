import libs.ArDb as ArDb
import unittest

class ArDbTestCase(unittest.TestCase):

    def testLoadSave(self):
        db = ArDb.Db("rsrc/tests/testLoadDb.db")
        db.save("rsrc/tests/testSaveDb.db")
        db2 = ArDb.Db("rsrc/tests/testSaveDb.db")
        self.assertTrue(db.equals(db2), "loaded and saved file")

    def testFold(self):
        db = ArDb.Db("rsrc/tests/testLoadDb.db")
        db2: 'ArDb.Db' = db.fold(lambda acc, next: acc.addRow(next), ArDb.Db())
        self.assertTrue(db.equals(db2), "test db fold")

    def testCreateDb(self):
        db = ArDb.Db()
        db.addRow(["1", "1", "1"])
        db.addRow(["2", "2"])
        db.addToRow(1, "2")
        db.addToLastRow("2")
        db2 = ArDb.Db()
        db2.createDb([["1","1","1"],["2","2","2","2"]])
        self.assertTrue(db.equals(db2), "test crate db")

    def testAddingToEmptyDb(self):
        db = ArDb.Db()
        db.addToLastRow('1')
        db2 = ArDb.Db()
        db2.createDb([['1']])
        self.assertTrue(db.equals(db2), "test create empty db")


if __name__ == '__main__':
    unittest.main()
