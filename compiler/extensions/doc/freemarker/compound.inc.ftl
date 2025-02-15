<#ftl output_format="HTML">
<#include "symbol.inc.ftl">
<#include "doc_comment.inc.ftl">
<#macro compound_fields fields columnCount indent>
    <#list fields as field>
        <@compound_field field, columnCount, indent/>
    </#list>
</#macro>

<#macro compound_field field columnCount indent>
    <#local I>${""?left_pad(indent * 2)}</#local>
    <#local typePrefix>
        <#if field.isVirtual>sql_virtual </#if><#t>
        <#if field.isAutoOptional>optional </#if><#t>
        <#if field.isArrayImplicit>implicit </#if><#t>
        <#if field.isArrayPacked>packed </#if><#t>
    </#local>
${I}<tbody class="anchor-group" id="${field.symbol.htmlLink.htmlAnchor}">
    <#if hasDocComments(field.docComments)>
${I}  <tr class="doc">
${I}    <td class="indent empty"></td>
${I}    <td colspan=${columnCount-1}>
          <@doc_comments_all field.docComments, indent+3/>
${I}    </td>
${I}  </tr>
    </#if>
    <#if field.alignmentExpression?has_content>
${I}  <tr>
${I}    <td colspan=${columnCount}>align(${field.alignmentExpression}):</td>
${I}  </tr>
    </#if>
    <#if field.offsetExpression?has_content>
${I}  <tr>
${I}    <td colspan=${columnCount}>${field.offsetExpression}:</td>
${I}  </tr>
    </#if>
${I}  <tr>
${I}    <td class="indent empty"></td>
${I}    <td>
${I}      ${typePrefix}<@compound_field_type_name field/>
${I}    </td>
${I}    <td>
${I}      <@symbol_reference field.symbol/><#rt>
            ${field.arrayRange}<#t>
    <#if field.initializerExpression?has_content>
            <#lt> = ${field.initializerExpression}<#rt>
    </#if>
    <#if field.optionalClauseExpression?has_content>
            <#lt> if ${field.optionalClauseExpression}<#rt>
    </#if>
    <#if field.constraintExpression?has_content>
             <#lt> : ${field.constraintExpression}<#rt>
    </#if>
    <#if field.sqlConstraintExpression?has_content>
             <#lt> sql ${field.sqlConstraintExpression}<#rt>
    </#if>
             <#lt>;
${I}    </td>
${I}  </tr>
${I}</tbody>
</#macro>

<#macro compound_functions functions columnCount indent>
    <#local I>${""?left_pad(indent * 2)}</#local>
    <#list functions as function>
${I}<tbody class="anchor-group" id="${function.symbol.htmlLink.htmlAnchor}">
    <#if hasDocComments(function.docComments)>
${I}  <tr class="doc">
${I}    <td class="indent empty"></td>
${I}    <td colspan=${columnCount-1}>
          <@doc_comments_all function.docComments, indent+3/>
${I}    </td>
${I}  </tr>
    </#if>
${I}  <tr>
${I}    <td class="indent empty"></td>
${I}    <td colspan=${columnCount-1}>
${I}      function <@symbol_reference function.returnSymbol/> <#rt>
            <#lt><@symbol_reference function.symbol/>()
${I}    </td>
${I}  </tr>
${I}  <tr>
${I}    <td class="indent empty"></td>
${I}    <td colspan=${columnCount-1}>{</td>
${I}  </tr>
${I}  <tr>
${I}    <td class="indent empty"></td>
${I}    <td colspan=${columnCount-1} class="indent">return ${function.resultExpression};</td>
${I}  </tr>
${I}  <tr>
${I}    <td class="indent empty"></td>
${I}    <td colspan=${columnCount-1}>}</td>
${I}  </tr>
${I}</tbody>
    </#list>
</#macro>

<#macro compound_template_parameters templateParameters>
    <#if templateParameters?has_content>
        &lt;<#t>
        <#list templateParameters as templateParameter>
            ${templateParameter.name}<#if templateParameter?has_next>, </#if><#t>
        </#list>
        &gt;<#t>
    </#if>
</#macro>

<#macro compound_parameters parameters>
    <#if parameters?has_content>
        (<#t>
        <#list parameters as parameter>
            <@symbol_reference parameter.symbol/> ${parameter.name}<#if parameter?has_next>, </#if><#t>
        </#list>
        )<#t>
    </#if>
</#macro>

<#macro compound_field_type_name field>
    <@symbol_reference field.typeSymbol/><@compound_field_type_arguments field.typeArguments/><#t>
    <#if field.dynamicBitFieldLengthExpression?has_content>
        &lt;${field.dynamicBitFieldLengthExpression}&gt;<#t>
    </#if>
</#macro>

<#macro compound_field_type_arguments typeArguments>
    <#if typeArguments?has_content>
        (<#t>
        <#list typeArguments as typeArgument>
            ${typeArgument}<#if typeArgument?has_next>, </#if><#t>
        </#list>
        )<#t>
    </#if>
</#macro>
