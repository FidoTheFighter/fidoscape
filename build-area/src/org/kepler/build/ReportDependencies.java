/*
 * Copyright (c) 2009 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */
package org.kepler.build;

import java.io.IOException;
import java.util.Vector;

import org.apache.tools.ant.taskdefs.optional.depend.ClassFile;
import org.apache.tools.ant.taskdefs.optional.depend.DirectoryIterator;
import org.kepler.build.modules.Module;
import org.kepler.build.modules.ModulesTask;

/**
 * Class to report dependencies for a module
 *
 * @auther welker
 */
public class ReportDependencies extends ModulesTask
{

    /**
     * run the task
     */
    @Override
    public void run() throws Exception
    {
        for (Module module : moduleTree)
        {
            report(module);
        }
    }

    /**
     * do the report
     *
     * @param module
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private void report(Module module) throws IOException
    {
        System.out.println(module);
        DirectoryIterator i = new DirectoryIterator(module.getDir(), false);

        ClassFile classFile = null;
        while ((classFile = i.getNextClassFile()) != null)
        {
            System.out.println(classFile.getFullClassName());
            Vector<String> v = classFile.getClassRefs();
            for (String s : v)
            {
                System.out.println("--" + s);
            }
        }

    }
}
