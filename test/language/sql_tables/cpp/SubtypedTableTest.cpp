#include <cstdio>
#include <string>
#include <fstream>
#include <memory>

#include "gtest/gtest.h"

#include "sql_tables/TestDb.h"

#include "zserio/SqliteFinalizer.h"

namespace sql_tables
{
namespace subtyped_table
{

using allocator_type = TestDb::allocator_type;
using string_type = zserio::string<zserio::RebindAlloc<allocator_type, char>>;

class SubtypedTableTest : public ::testing::Test
{
public:
    SubtypedTableTest()
    {
        std::remove(DB_FILE_NAME);

        m_database = new sql_tables::TestDb(DB_FILE_NAME);
        m_database->createSchema();
    }

    ~SubtypedTableTest()
    {
        delete m_database;
    }

protected:
    bool isTableInDb()
    {
        string_type checkTableName = "subtypedTable";
        string_type sqlQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + checkTableName +
                "'";
        std::unique_ptr<sqlite3_stmt, zserio::SqliteFinalizer> statement(
                m_database->connection().prepareStatement(sqlQuery));

        int result = sqlite3_step(statement.get());
        if (result == SQLITE_DONE || result != SQLITE_ROW)
            return false;

        const unsigned char* readTableName = sqlite3_column_text(statement.get(), 0);
        if (readTableName == nullptr ||
                checkTableName.compare(reinterpret_cast<const char*>(readTableName)) != 0)
        {
            return false;
        }

        return true;
    }

    static const char DB_FILE_NAME[];

    sql_tables::TestDb* m_database;
};

const char SubtypedTableTest::DB_FILE_NAME[] = "language/sql_tables/subtyped_table_test.sqlite";

TEST_F(SubtypedTableTest, testSubtypedTable)
{
    ASSERT_TRUE(isTableInDb());

    TestTable& studentsAsTestTable = m_database->getSubtypedTable();
    SubtypedTable& studentsAsSubtypedTable = m_database->getSubtypedTable();
    ASSERT_EQ(&studentsAsTestTable, &studentsAsSubtypedTable);
}

} // namespace subtyped_table
} // namespace sql_tables
