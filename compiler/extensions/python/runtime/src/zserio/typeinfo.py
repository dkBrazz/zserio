"""
The module contains classes for type info.
"""

import typing
import enum

class TypeInfo:
    """
    Type info class which provides information about generated types.
    """

    def __init__(self, schema_name: str, py_type: typing.Type, *,
                 attributes: typing.Dict['TypeAttribute', typing.Any] = None):
        """
        Type info constructor.

        :param schema_name Zserio schema full type name.
        :param py_type: Reference to the generated type.
        :param attributes: List of type attributes.
        """

        self._schema_name = schema_name
        self._py_type = py_type
        self._attributes = attributes if attributes is not None else {}

    @property
    def schema_name(self) -> str:
        """
        Returns the full type name as is defined in Zserio schema.

        :returns: Zserio schema full type name.
        """

        return self._schema_name

    @property
    def py_type(self) -> typing.Type:
        """
        Gets Python type generated for this Zserio type.

        :returns: Python type.
        """

        return self._py_type

    @property
    def attributes(self) -> typing.Dict['TypeAttribute', typing.Any]:
        """
        Gets dictionary with type attributes.

        Attribute is a an arbitrary value which type is given by the key, which is TypeAttribute enumeration.

        * `(TypeAttribute.UNDERLYING_TYPE, TypeInfo(...))`

          * denotes that the type has an underlying type (e.g. enum or bitmask),
            the value is a TypeInfo of the underlying type

        * `(TypeAttribute.UDNERLYING_TYPE_ARGUMENTS, ['5'])`

          * keeps type arguments of the underlying type when it is a dynamic bit field

        * `(TypeAttribute.ENUM_ITEMS, [ItemInfo(...), ItemInfo(...), ...])`

          * denotes that the type is an enumeration, the value contains list of enum items ItemInfo

        * `(TypeAttribute.BITMASK_VALUES, [ItemInfo(...), ItemInfo(...), ...])`

          * denotes that the type is a bitmask, the value contains list of bitmask values ItemInfo

        * `(TypeAttribute.FIELDS,  [MemberInfo(...), MemberInfo(...), ...])`

          * denotes that the type is a compound type, the value contains list of fields MemberInfo,
            the attribute is present even for empty compounds and then it contains the empty list

        * `(TypeAttribute.PARAMETERS, [MemberInfo(...), MemberInfo(...), ...])`

          * denotes that the compound type is parameterized type, the value contains non-empty list of
            parameters MemberInfo, for non-parameterized types the attribute is not present

        * `(TypeAttribute.FUNCTIONS, [MemberInfo(...), MemberInfo(...), ...])`

          * denotes that the compound type has functions, the value contains non-empty list of functions
            MemberInfo, for compounds without functions the attribute is not present

        * `(TypeAttribute.SELECTOR, None`) `(TypeAttribute.SELECTOR, 'param1')`

          * denotes that the type is either a union (when the value is None) or choice when the
            value contains the selector expression

        * `(TypeAttribute.CASES, [CaseInfo(...), CaseInfo(...), ...])`

          * denotes that the type is a choice, the value contains list of CaseInfo for each choice case
          * note that the TypeAttribute.FIELDS attribute is present also in choices

        * `(TypeAttribute.TEMPLATE_NAME, 'TemplatedStructure')`

          * denotes that the type is a template instantiation, the value contains the template name

        * `(TypeAttribute.TEMPLATE_ARGUMENTS, [test.TemplateArg.type_info(), ...])`

          * present when the type is a template instantiation, the value contains list of template arguments
            TypeInfo

        * `(TypeAttribute.COLUMNS, [MemberInfo(...), MemberInfo(...), ...])`

          * denotes that the type is a SQL table, the value contains list of columns MemberInfo

        * `(TypeAttribute.TABLES, [MemberInfo(...), MemberInfo(...), ...])`

          * denotes that the type is a SQL database, the value contain list of tables MemberInfo

        * `(TypeAttribute.SQL_CONSTRAINT, 'PRIMARY KEY(columnA)')`

          * denotes that the SQL table contains a SQL constraint

        * `(TypeAttribute.VIRTUAL_TABLE_USING, 'fts4')`

          * denotes that the SQL table is a virtual table, the value contains the used virtual table module

        * `(TypeAttribute.WITHOUT_ROWID, None)`

          * denotes that the SQL table is a WITHOUT ROWID table, the value is always None

        * `(TypeAttribute.MESSAGES, [MemberInfo(...), MemberInfo(...), ...])`

          * denotes that the type is a pub-sub, the value contains list of messages MemberInfo

        * `(TypeAttribute.METHODS, [MemberInfo(...), MemberInfo(...), ...])`

          * denotes that the type is a service, the value contains list of methods MemberInfo

        :returns Type attributes.
        """

        return self._attributes

class TypeAttribute(enum.Enum):
    """
    Type attribute type to be used in TypeInfo.

    Determines type of the second element in the attribute tuple returned in attributes list from TypeInfo.
    """

    UNDERLYING_TYPE = enum.auto()
    UNDERLYING_TYPE_ARGUMENTS = enum.auto()
    ENUM_ITEMS = enum.auto()
    BITMASK_VALUES = enum.auto()
    FIELDS = enum.auto()
    PARAMETERS = enum.auto()
    FUNCTIONS = enum.auto()
    SELECTOR = enum.auto()
    CASES = enum.auto()
    TEMPLATE_NAME = enum.auto()
    TEMPLATE_ARGUMENTS= enum.auto()
    COLUMNS = enum.auto()
    TABLES = enum.auto()
    SQL_CONSTRAINT = enum.auto()
    VIRTUAL_TABLE_USING = enum.auto()
    WITHOUT_ROWID = enum.auto()
    MESSAGES = enum.auto()
    METHODS = enum.auto()

class MemberInfo:
    """
    Member info class which provides information about members of compound types.
    """

    def __init__(self, schema_name: str, typeinfo: TypeInfo, *,
                 attributes: typing.Dict['MemberAttribute', typing.Any] = None):
        """
        Member info constructor.

        :param schema_name: Name of the member as is defined in Zserio schema.
        :param type_info: Type info of the member.
        :param attributes: List of member attributes.
        """

        self._schema_name = schema_name
        self._type_info = typeinfo
        self._attributes = attributes if attributes is not None else {}

    @property
    def schema_name(self) -> str:
        """
        Gets name of the member as is defined in Zserio schema.

        :returns: Member name in Zserio schema.
        """

        return self._schema_name

    @property
    def type_info(self) -> TypeInfo:
        """
        Gets type info of this member.

        :returns: Type info.
        """

        return self._type_info

    @property
    def attributes(self) -> typing.Dict['MemberAttribute', typing.Any]:
        """
        Gets dictionary with member attributes.

        Attribute is a an arbitrary value which type is given by the key, which is MemberAttribute enumeration.
        All expressions are stored as strings.

        **Possible attributes:**

        * `(MemberAttribute.PROPERTY_NAME, 'field1')`

          * contains name of the property generated in Python

        * `(MemberAttribute.TYPE_ARGUMENTS, ['field1', '10', ...])`

          * keeps type arguments for parameterized types or dynamic bit fields, the value contains list of
            strings containing particular arguments expressions

        * `(MemberAttribute.ALING, '8')`

          * denotes that the member field has an alignment, the value contains the alignment expression

        * `(MemberAttribute.OFFSET, 'offsetField')`

          * denotes that the member field has an offset, the value contains the offset expression

        * `(MemberAttribute.OPTIONAL, None)`, `(MemberAttribute.OPTIONAL, 'field1 != 0')`

          * denotes that the member is an optional, when the value is None, then it's an auto optional,
            otherwise it contains the optional clause

        * `(MemberAttribute.CONSTRAINT, 'field > 10')`

          * denotes that the member has a constraint, the value contains the constraint expression

        * `(MemberAttribute.FUNCTION_NAME, 'function_name')`

          * keeps the generated function name

        * `MemberAttribute.FUNCTION_RESULT, 'field1 + 5')`

          * keeps the result expression of a function

        * `(MemberAttribute.ARRAY_LENGTH, None)`, `(MemberAttribute.ARRAY_LENGTH, 'field1 + 10')`

          * denotes that the member is an array, when the value is None, then it's an auto array,
            otherwise it contains the length expression

        * `(MemberAttribute.IMPLICIT, None)`

          * denotes that the member is an implicit array, the value is always None

        * `(MemberAttribute.PACKED, None)`

          * denotes that the member is a packed array, the value is always None

        * `(MemberAttribute.SQL_TYPE_NAME, 'INTEGER')`

          * keeps SQLite type name used for this column

        * `(MemberAttribute.SQL_CONSTRAINT, 'PRIMARY KEY NOT NULL')`

          * denotes that the member has a SQL constraint

        * `(MemberAttribute.VIRTUAL, None)`

          * denotes that the column in a SQL table is virtual

        * `(MemberAttribute.TOPIC, 'topic/definition')`

          * keeps the topic definition of a pub-sub message

        * `(MemberAttribute.PUBLISH, 'publish_message_name')`

          * denotes that the pub-sub message is published, the value contains the publishing method name

        * `(MemberAttribute.SUBSCRIBE, 'subscribe_message_name')`

          * denotes that the pub-sub message is subscribed, the value contains the subscribing method name

        * `(MemberAttribute.CLIENT_METHOD_NAME, 'client_method_name')`

          * keeps the name of the method in the generated Client class

        * `(MemberAttribute.REQUEST_TYPE, request_type.type_info())`

          * keeps the request type TypeInfo, note that response type is in the method TypeInfo

        :returns: Member attributes.
        """

        return self._attributes

class MemberAttribute(enum.Enum):
    """
    Member attribute type to be used in MemberInfo.

    Determines type of the second element in the attribute tuple returned in attributes list from MemberInfo.
    """

    PROPERTY_NAME = enum.auto()
    TYPE_ARGUMENTS = enum.auto()
    ALIGN = enum.auto()
    OFFSET = enum.auto()
    INITIALIZER = enum.auto()
    OPTIONAL = enum.auto()
    CONSTRAINT = enum.auto()
    FUNCTION_NAME = enum.auto()
    FUNCTION_RESULT = enum.auto()
    ARRAY_LENGTH = enum.auto()
    IMPLICIT = enum.auto()
    PACKED = enum.auto()
    SQL_TYPE_NAME = enum.auto()
    SQL_CONSTRAINT = enum.auto()
    VIRTUAL = enum.auto()
    TOPIC = enum.auto()
    PUBLISH = enum.auto()
    SUBSCRIBE = enum.auto()
    CLIENT_METHOD_NAME = enum.auto()
    REQUEST_TYPE = enum.auto()

class CaseInfo:
    """
    Case info class which provides information about choice cases in generated choices.
    """

    def __init__(self, case_expressions: typing.List[str], field: typing.Optional[MemberInfo]):
        """
        Constructor.

        :param case_expressions: List of case expression in the choice case. When empty, it's a default case.
        :param field: Field associated with the choice case, can be empty.
        """

        self._case_expressions = case_expressions
        self._field = field

    @property
    def case_expressions(self):
        """
        Gets case expressions in the choice case. An empty list denotes the default case.

        :returns: List of case expressions.
        """

        return self._case_expressions

    @property
    def field(self):
        """
        Gets field associated with the choice case. Can be empty.

        :returns: Field MemberInfo.
        """

        return self._field


class ItemInfo:
    """
    Item info class which provides information about items of generated enumerable types.
    """

    def __init__(self, schema_name: str, py_item: typing.Any):
        """
        Constructor.

        :param schema_name: Name of the item as is defined in Zserio schema.
        :param py_item: Reference to the generated item.
        """

        self._schema_name = schema_name
        self._py_item = py_item

    @property
    def schema_name(self):
        """
        Gets name of the item as is defined in Zserio schema.

        :returns: Item name in Zserio schema.
        """

        return self._schema_name

    @property
    def py_item(self):
        """
        Gets reference to the item generated in Python.

        :returns: Python item.
        """

        return self._py_item
