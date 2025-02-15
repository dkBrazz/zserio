package zserio.extension.java;

import java.util.ArrayList;
import java.util.List;

import zserio.ast.CompoundType;
import zserio.ast.TypeReference;
import zserio.ast.Function;
import zserio.extension.common.ExpressionFormatter;
import zserio.extension.common.ZserioExtensionException;

public final class CompoundFunctionTemplateData
{
    public CompoundFunctionTemplateData(JavaNativeMapper javaNativeMapper, CompoundType compoundType,
            ExpressionFormatter javaExpressionFormatter) throws ZserioExtensionException
    {
        compoundFunctionList = new ArrayList<CompoundFunction>();
        final Iterable<Function> functionList = compoundType.getFunctions();
        for (Function compoundFunction : functionList)
        {
            compoundFunctionList.add(new CompoundFunction(javaNativeMapper, compoundFunction,
                    javaExpressionFormatter));
        }
    }

    public Iterable<CompoundFunction> getList()
    {
        return compoundFunctionList;
    }

    public static class CompoundFunction
    {
        public CompoundFunction(JavaNativeMapper javaNativeMapper, Function function,
                ExpressionFormatter javaExpressionFormatter) throws ZserioExtensionException
        {
            final TypeReference returnTypeReference = function.getReturnTypeReference();
            returnTypeName = javaNativeMapper.getJavaType(returnTypeReference).getFullName();
            name = AccessorNameFormatter.getFunctionName(function);
            resultExpression = javaExpressionFormatter.formatGetter(function.getResultExpression());
        }

        public String getReturnTypeName()
        {
            return returnTypeName;
        }

        public String getName()
        {
            return name;
        }

        public String getResultExpression()
        {
            return resultExpression;
        }

        private final String returnTypeName;
        private final String name;
        private final String resultExpression;
    }

    private final List<CompoundFunction>    compoundFunctionList;
}
