<#include "FileHeader.inc.ftl">
<#include "CompoundConstructor.inc.ftl">
<#include "CompoundParameter.inc.ftl">
<#include "CompoundField.inc.ftl">
<#include "CompoundFunction.inc.ftl">
<@file_header generatorDescription/>

#include <zserio/StringConvertUtil.h>
#include <zserio/CppRuntimeException.h>
#include <zserio/HashCodeUtil.h>
#include <zserio/BitPositionUtil.h>
#include <zserio/BitSizeOfCalculator.h>
#include <zserio/BitFieldUtil.h>
<#if has_field_with_constraint(fieldList)>
#include <zserio/ConstraintException.h>
</#if>
<@system_includes cppSystemIncludes/>

<@user_include package.path, "${name}.h"/>
<@user_includes cppUserIncludes, false/>
<@namespace_begin package.path/>

<@inner_classes_definition name, fieldList/>
<#if withWriterCode>
<#macro empty_constructor_field_initialization>
        m_objectChoice(allocator)
</#macro>
<#assign emptyConstructorInitMacroName><#if fieldList?has_content>empty_constructor_field_initialization</#if></#assign>
    <@compound_constructor_definition compoundConstructorsData emptyConstructorInitMacroName/>

</#if>
<#macro read_constructor_field_initialization packed>
        m_objectChoice(readObject(<#if packed>contextNode, </#if>in, allocator), allocator)
</#macro>
<#assign readConstructorInitMacroName><#if fieldList?has_content>read_constructor_field_initialization</#if></#assign>
<@compound_read_constructor_definition compoundConstructorsData, readConstructorInitMacroName/>
<@compound_read_constructor_definition compoundConstructorsData, readConstructorInitMacroName, true/>

<#if needs_compound_initialization(compoundConstructorsData) || has_field_with_initialization(fieldList)>
<@compound_copy_constructor_definition compoundConstructorsData/>

<@compound_assignment_operator_definition compoundConstructorsData/>

<@compound_move_constructor_definition compoundConstructorsData/>

<@compound_move_assignment_operator_definition compoundConstructorsData/>

</#if>
<@compound_allocator_propagating_copy_constructor_definition compoundConstructorsData/>

<#if needs_compound_initialization(compoundConstructorsData)>
<@compound_initialize_definition compoundConstructorsData, needsChildrenInitialization/>

</#if>
<#macro choice_selector_condition expressionList>
    <#if expressionList?size == 1>
        selector == (${expressionList?first})<#t>
    <#else>
        <#list expressionList as expression>
        (selector == (${expression}))<#if expression?has_next> || </#if><#t>
        </#list>
    </#if>
</#macro>
<#macro choice_switch memberActionMacroName needsBreak packed=false>
    <#local fieldIndex=0>
    <#if canUseNativeSwitch>
    switch (${selectorExpression})
    {
        <#list caseMemberList as caseMember>
            <#list caseMember.expressionList as expression>
    case ${expression}:
            </#list>
        <@.vars[memberActionMacroName] caseMember, packed, fieldIndex/>
            <#if caseMember.compoundField??><#local fieldIndex+=1></#if>
            <#if needsBreak>
        break;
            </#if>
        </#list>
        <#if !isDefaultUnreachable>
    default:
            <#if defaultMember??>
        <@.vars[memberActionMacroName] defaultMember, packed, fieldIndex/>
                <#if defaultMember.compoundField??><#local fieldIndex+=1></#if>
                <#if needsBreak>
        break;
                </#if>
            <#else>
        throw ::zserio::CppRuntimeException("No match in choice ${name}!");
            </#if>
        </#if>
    }
    <#else>
    const auto selector = ${selectorExpression};

        <#list caseMemberList as caseMember>
            <#if caseMember?has_next || !isDefaultUnreachable>
    <#if caseMember?index != 0>else </#if>if (<@choice_selector_condition caseMember.expressionList/>)
            <#else>
    else
            </#if>
    {
        <@.vars[memberActionMacroName] caseMember, packed, fieldIndex/>
            <#if caseMember.compoundField??><#local fieldIndex+=1></#if>
    }
        </#list>
        <#if !isDefaultUnreachable>
    else
    {
            <#if defaultMember??>
        <@.vars[memberActionMacroName] defaultMember, packed, fieldIndex/>
                <#if defaultMember.compoundField??><#local fieldIndex+=1></#if>
            <#else>
        throw ::zserio::CppRuntimeException("No match in choice ${name}!");
            </#if>
    }
        </#if>
    </#if>
</#macro>
<#macro choice_initialize_children_member member packed index>
    <#if member.compoundField??>
        <@compound_initialize_children_field member.compoundField, 2/>
    <#else>
        // empty
    </#if>
</#macro>
<#if needsChildrenInitialization>
void ${name}::initializeChildren()
{
    <#if fieldList?has_content>
    <@choice_switch "choice_initialize_children_member", true/>
    </#if>
    <@compound_initialize_children_epilog_definition compoundConstructorsData/>
}

</#if>
<@compound_parameter_accessors_definition name, compoundParametersData/>
<#list fieldList as field>
    <#if needs_field_getter(field)>
<@field_raw_cpp_type_name field/>& ${name}::${field.getterName}()
{
    return m_objectChoice.get<<@field_cpp_type_name field/>>()<#if field.array??>.getRawArray()</#if>;
}

    </#if>
<@field_raw_cpp_argument_type_name field/> ${name}::${field.getterName}() const
{
    return m_objectChoice.get<<@field_cpp_type_name field/>>()<#if field.array??>.getRawArray()</#if>;
}

    <#if needs_field_setter(field)>
void ${name}::${field.setterName}(<@field_raw_cpp_argument_type_name field/> <@field_argument_name field/>)
{
    m_objectChoice = <@compound_setter_field_value field/>;
}

    </#if>
    <#if needs_field_rvalue_setter(field)>
void ${name}::${field.setterName}(<@field_raw_cpp_type_name field/>&& <@field_argument_name field/>)
{
    m_objectChoice = <@compound_setter_field_rvalue field/>;
}

    </#if>
</#list>
<@compound_functions_definition name, compoundFunctionsData/>
void ${name}::createPackingContext(${types.packingContextNode.name}&<#if fieldList?has_content> contextNode</#if>)
{
<#list fieldList as field>
    <@compound_create_packing_context_field field/>
</#list>
}

<#macro init_packing_context_member member packed index>
    <#if member.compoundField??>
        <@compound_init_packing_context_field member.compoundField, index, 2/>
    <#else>
        // empty
    </#if>
</#macro>
void ${name}::initPackingContext(${types.packingContextNode.name}&<#rt>
        <#lt><#if needs_packing_context_node(fieldList)> contextNode</#if>) const
{
<#if needs_packing_context_node(fieldList)>
    <@choice_switch "init_packing_context_member", true, true/>
</#if>
}

<#macro choice_bitsizeof_member member packed index>
    <#if member.compoundField??>
        <@compound_bitsizeof_field member.compoundField, 2, packed, index/>
    <#else>
        // empty
    </#if>
</#macro>
size_t ${name}::bitSizeOf(size_t<#if fieldList?has_content> bitPosition</#if>) const
{
<#if fieldList?has_content>
    size_t endBitPosition = bitPosition;

    <@choice_switch "choice_bitsizeof_member", true/>

    return endBitPosition - bitPosition;
<#else>
    return 0;
</#if>
}

size_t ${name}::bitSizeOf(${types.packingContextNode.name}&<#rt>
        <#if needs_packing_context_node(fieldList)> contextNode</#if>, <#t>
        <#lt>size_t<#if fieldList?has_content> bitPosition</#if>) const
{
<#if fieldList?has_content>
    size_t endBitPosition = bitPosition;

    <@choice_switch "choice_bitsizeof_member", true, true/>

    return endBitPosition - bitPosition;
<#else>
    return 0;
</#if>
}
<#if withWriterCode>

<#macro choice_initialize_offsets_member member packed index>
    <#if member.compoundField??>
        <@compound_initialize_offsets_field member.compoundField, 2, packed, index/>
    <#else>
        // empty
    </#if>
</#macro>
size_t ${name}::initializeOffsets(size_t bitPosition)
{
    <#if fieldList?has_content>
    size_t endBitPosition = bitPosition;

    <@choice_switch "choice_initialize_offsets_member", true/>

    return endBitPosition;
    <#else>
    return bitPosition;
    </#if>
}

size_t ${name}::initializeOffsets(${types.packingContextNode.name}&<#rt>
        <#if needs_packing_context_node(fieldList)> contextNode</#if>, <#t>
        <#lt>size_t bitPosition)
{
    <#if fieldList?has_content>
    size_t endBitPosition = bitPosition;

    <@choice_switch "choice_initialize_offsets_member", true, true/>

    return endBitPosition;
    <#else>
    return bitPosition;
    </#if>
}
</#if>

<#macro choice_compare_member member packed index>
    <#if member.compoundField??>
        return (!m_objectChoice.hasValue() && !other.m_objectChoice.hasValue()) ||
                (m_objectChoice.hasValue() && other.m_objectChoice.hasValue() &&
                m_objectChoice.get<<@field_cpp_type_name member.compoundField/>>() == <#rt>
                <#lt>other.m_objectChoice.get<<@field_cpp_type_name member.compoundField/>>());
    <#else>
        return true; // empty
    </#if>
</#macro>
bool ${name}::operator==(const ${name}& other) const
{
    if (this == &other)
        return true;

    <@compound_parameter_comparison_with_any_holder compoundParametersData/>
    <#if fieldList?has_content>
    <@choice_switch "choice_compare_member", false/>
    <#else>
    return true;
    </#if>
}

<#macro choice_hash_code_member member packed index>
    <#if member.compoundField??>
        if (m_objectChoice.hasValue())
            result = ::zserio::calcHashCode(result, m_objectChoice.get<<@field_cpp_type_name member.compoundField/>>());
    <#else>
        // empty
    </#if>
</#macro>
uint32_t ${name}::hashCode() const
{
    uint32_t result = ::zserio::HASH_SEED;

    <@compound_parameter_hash_code compoundParametersData/>
    <#if fieldList?has_content>
    <@choice_switch "choice_hash_code_member", true/>
    </#if>

    return result;
}
<#if withWriterCode>

<#macro choice_write_member member packed index>
    <#if member.compoundField??>
        <@compound_write_field member.compoundField, name, 2, packed, index/>
    <#else>
        // empty
    </#if>
</#macro>
<#assign hasPreWriteAction=needsChildrenInitialization || hasFieldWithOffset/>
void ${name}::write(::zserio::BitStreamWriter&<#if fieldList?has_content> out</#if>, <#rt>
        ::zserio::PreWriteAction<#if hasPreWriteAction> preWriteAction</#if>)<#lt>
{
    <#if fieldList?has_content>
        <#if hasPreWriteAction>
    <@compound_pre_write_actions needsChildrenInitialization, hasFieldWithOffset/>

        </#if>
    <@choice_switch "choice_write_member", true/>
    </#if>
}

void ${name}::write(${types.packingContextNode.name}&<#rt>
        <#if needs_packing_context_node(fieldList)> contextNode</#if>, <#t>
        ::zserio::BitStreamWriter&<#if fieldList?has_content> out</#if>)<#t>
{
    <#if fieldList?has_content>
    <@choice_switch "choice_write_member", true, true/>
    </#if>
}
</#if>
<#if fieldList?has_content>

<#macro choice_read_member member packed index>
    <#if member.compoundField??>
        <#if needs_field_read_local_variable(member.compoundField)>
        {
            <@compound_read_field member.compoundField, name, 3, packed, index/>
        }
        <#else>
        <@compound_read_field member.compoundField, name, 2, packed, index/>
        </#if>
    <#else>
        return ${types.anyHolder.name}(allocator);
    </#if>
</#macro>
${types.anyHolder.name} ${name}::readObject(::zserio::BitStreamReader& in, const allocator_type& allocator)
{
    <@choice_switch "choice_read_member", false/>
}

${types.anyHolder.name} ${name}::readObject(${types.packingContextNode.name}&<#rt>
        <#lt><#if needs_packing_context_node(fieldList)> contextNode</#if>,
        ::zserio::BitStreamReader& in, const allocator_type& allocator)
{
    <@choice_switch "choice_read_member", false, true/>
}

<#macro choice_copy_object member packed index>
    <#if member.compoundField??>
        return ::zserio::allocatorPropagatingCopy<<@field_cpp_type_name member.compoundField/>>(m_objectChoice, allocator);
    <#else>
        return ${types.anyHolder.name}(allocator);
    </#if>
</#macro>
${types.anyHolder.name} ${name}::copyObject(const allocator_type& allocator) const
{
    <@choice_switch "choice_copy_object", false/>
}
</#if>
<@namespace_end package.path/>
