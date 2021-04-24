<#ftl encoding="ISO-8859-1" />
<#---
    File header
    @copyright bla bla bla 2021
-->

<#import "lib/test_lib.ftl" as lib />

<#--- Macro without category
    @param {String} foo A foo string
    @param {Sequence} [baz] A list of baz things
-->
<#macro fooBar2000 foo baz=[]>
    FooBar2000 ${foo!}
    <#list baz as ba>
        ${ba}
    </#list>
</#macro>

<#--********************************************************************************** -->
<#-- @begin Opening a section -->
<#--********************************************************************************** -->

<#---
    Macro that put a value on a array
    @deprecated Becasue yes
    @param {String} arrayVar Variable name that must be a sequence
    @param [content=""] Content to be appened to the array pointer by arrayVar
-->
<#macro putOnArray arrayVar content="">
    <#if content?is_string && content == "">
        <#local content>
            <#nested />
        </#local>
    </#if>
    <#-- freemarker doing some metaprogramming -->
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
    Macro to generate HTML attributes from a hash containing <AttributeName : Value>
    Attribute names are normalized, replacing '_' by '-'
    @param {Hash} vars  Map with pairs of attribute name - value
    @param [exclude] A list of attributes to be ignored
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
    @param {Hash<String,String>} data - Map with pairs of XXX field - value
 -->
<#macro data_atribs data={}>
    <#if data?is_hash>
        <#list data as key, value>
            <#if value?is_number || value?is_boolean><#local temp=value?c /><#else><#local temp=value?string /></#if>
            data-${key}="${temp}"
        </#list>
    </#if>
</#macro>

<#-- @end -->
