/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codahale.passpol.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.quicktheories.quicktheories.QuickTheory.qt;
import static org.quicktheories.quicktheories.generators.SourceDSL.arbitrary;
import static org.quicktheories.quicktheories.generators.SourceDSL.strings;

import com.codahale.passpol.PasswordPolicy;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

  @Test
  void validPasswords() throws Exception {
    final PasswordPolicy policy = new PasswordPolicy(8, 64);
    qt().forAll(strings().allPossible().ofLengthBetween(20, 30))
        .check(policy);
    qt().forAll(strings().ascii().ofLengthBetween(8, 64))
        .check(policy);
  }

  @Test
  void shortPasswords() throws Exception {
    final PasswordPolicy policy = new PasswordPolicy(10, 64);
    qt().forAll(strings().ascii().ofLengthBetween(1, 9))
        .check(policy.negate());
  }

  @Test
  void longPasswords() throws Exception {
    final PasswordPolicy policy = new PasswordPolicy(8, 20);
    qt().forAll(strings().ascii().ofLengthBetween(21, 30))
        .check(policy.negate());
  }

  @Test
  void weakPasswords() throws Exception {
    final PasswordPolicy policy = new PasswordPolicy();
    qt().forAll(arbitrary().sequence("password", "liverpool"))
        .check(policy.negate());
  }

  @Test
  void normalize() throws Exception {
    final PasswordPolicy policy = new PasswordPolicy();
    assertArrayEquals(new byte[]{-61, -124, 102, 102, 105, 110}, policy.normalize("Ä\uFB03n"));

    qt().forAll(strings().basicLatinAlphabet().ofLengthBetween(10, 20))
        .check(s -> s.equals(new String(policy.normalize(s), StandardCharsets.UTF_8)));
  }
}