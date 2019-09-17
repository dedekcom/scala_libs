from tests import TestExtList, TestArDb
import unittest


if __name__ == '__main__':
    suite1 = unittest.TestLoader().loadTestsFromTestCase(TestExtList.ExtListTestCase)
    unittest.TextTestRunner(verbosity=2).run(suite1)

    suite2 = unittest.TestLoader().loadTestsFromTestCase(TestArDb.ArDbTestCase)
    unittest.TextTestRunner(verbosity=2).run(suite2)
