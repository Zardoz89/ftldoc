<#ftl encoding="ISO-8859-1" />
<#---
    File header
    @copyright bla bla bla 2021
-->

<#--********************************************************************************** -->
<#-- @begin Opening a section -->
<#--********************************************************************************** -->

<#---
    Macro that put a value on a array
    @param arrayVar {String} Variable name that must be a sequence
    @param content (Optinal) Content to be appened to the array pointer by arrayVar
-->
<#macro putOnArray arrayVar content="">
    <#if content?is_string && content == "">
        <#local content>
            <#nested />
        </#local>
    </#if>
    <#-- freemarker parseando una cadena para generar codigo freemarker que va a ejecutar (metaprograming) -->
    <#if content?is_string>
        <@"<#assign ${arrayVar} = (${arrayVar}![]) + [content]>"?interpret />
    <#elseif content?is_enumerable>
        <@"<#assign ${arrayVar} = (${arrayVar}![]) + content>"?interpret />
    </#if>
</#macro>

<#-- @end -->

<#--*********************************************************************** -->
<#-- @begin Opening another section -->
<#--*********************************************************************** -->
<#---
    Macro to generate HTML attributes from a hash cintaint <AttributeName : Value>
    Attribute names are normalized, replacing '_' by '-'
    @param vars {Hash} Map with pais attribute name - value
    @param exclude (Optional) A list of attrbute to be ignored
 -->
<#macro carry_atribs vars exclude=[]>
    <@compress single_line=true>
        <#if vars?is_sequence><#return /></#if>
        <#list vars?keys as k>
            <#if !exclude?seq_contains(k) && (k != "temp")
                    && (k != "data") && (k != "attrs") && (k != "dynattrs") && (k != "aria")>
                <#if vars[k]?is_sequence>
                    <#if vars[k]?size &gt; 0><#t>
                    ${k?replace('_', '-')}="<#t>
                    <#list vars[k] as temp><#t>
                        <#if temp != ""><#t>
                            <#if temp?is_number || temp?is_boolean ><#local temp=temp?c /></#if><#t>
                            ${temp} <#t>
                        </#if><#t>
                    </#list><#t>
                    "<#t>
                    </#if>
                <#else>
                    <#if vars[k]?is_number || vars[k]?is_boolean ><#local temp=vars[k]?c /><#t>
                    <#else><#local temp = vars[k]?string /><#t>
                    </#if><#t>
                    <#if temp != "">
                        ${k?replace('_', '-')}="${temp}"
                    </#if>
                </#if>
            </#if>
        </#list>
    </@compress>
</#macro>

<#---
    Macro to generate data-XXX HTML attributes
    @param data {Hash} Map with pais XXX field - value
 -->
<#macro data_atribs data={}>
    <#if data?is_hash>
        <#list data as key, value>
            <#if value?is_number || value?is_boolean><#local temp=value?c /><#else><#local temp=value?string /></#if>
            data-${key}="${temp}"
        </#list>
    </#if>
</#macro>

<#---
    Function that emulates C/Java '?' operator
-->
<#function iif condicion valor1 valor2>
    <#if condicion>
        <#return valor1 />
    <#else>
        <#return valor2 />
    </#if>
</#function>

<#---
    Function that generates a string from a sequence, putting a separator bettwen elements
    @param seq {Sequence} Sequecen to be converted to String
    @param separador (Optional) Seperator element. By default it's a space (" ")
-->
<#function join seq separator = " ">
    <#local res = "" />
    <#list seq as e>
        <#if e_index == 0>
            <#local res = res + e />
        <#else>
            <#local res = res + separator + e />
        </#if>
    </#list>
    <#return res />
</#function>

<#---
    Function that converts a value to a Boolean
    @param Input String or Number to be converted to Boolean
-->
<#function toBool input>
    <#if input?is_boolean>
        <#return input />
    </#if>

    <#-- Number -->
    <#if input?is_number>
        <#if input != 0>
            <#return true />
        <#else>
            <#return false />
        </#if>
    </#if>

    <#-- String -->
    <#if input?is_string>
        <#if input == "" || input == "false"|| input == "not"  || input == "no" || input == "n" || input == "f" >
            <#return false />
        <#else>
            <#return true />
        </#if>
    </#if>
    
    <#-- != Null or not has content -->
    <#if input?? && input?has_content>
        <#return true />
    <#else>
        <#return false />
    </#if>
</#function>

<#---
    Checks if a String value it's a number
-->
<#function isStringNumber variable>
    <#attempt>
      <#assign numberVariable = variable?number />
      <#return numberVariable?is_number />
    <#recover>
      <#return false />
    </#attempt>
</#function>

<#-- @end -->